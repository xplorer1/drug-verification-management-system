package com.pharma.drugverification.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharma.drugverification.config.TestConfig;
import com.pharma.drugverification.domain.Batch;
import com.pharma.drugverification.domain.Drug;
import com.pharma.drugverification.domain.User;
import com.pharma.drugverification.dto.*;
import com.pharma.drugverification.repository.UserRepository;
import com.pharma.drugverification.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class DrugLifecycleIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        private String manufacturerToken;
        private String regulatorToken;
        private String pharmacistToken;

        private Long manufacturerId;
        private Long regulatorId;
        private Long pharmacistId;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();

                User manufacturer = new User();
                manufacturer.setUsername("manufacturer");
                manufacturer.setPasswordHash("hashed");
                manufacturer.setEmail("man@pharma.com");
                manufacturer.setFullName("Manufacturer User");
                manufacturer.setRole(User.UserRole.MANUFACTURER);
                manufacturer = userRepository.save(manufacturer);
                manufacturerId = manufacturer.getId();
                manufacturerToken = "Bearer "
                                + jwtTokenProvider.generateToken(manufacturerId, "manufacturer", "MANUFACTURER");

                User regulator = new User();
                regulator.setUsername("regulator");
                regulator.setPasswordHash("hashed");
                regulator.setEmail("reg@gov.com");
                regulator.setFullName("Regulator User");
                regulator.setRole(User.UserRole.REGULATOR);
                regulator = userRepository.save(regulator);
                regulatorId = regulator.getId();
                regulatorToken = "Bearer " + jwtTokenProvider.generateToken(regulatorId, "regulator", "REGULATOR");

                User pharmacist = new User();
                pharmacist.setUsername("pharmacist");
                pharmacist.setPasswordHash("hashed");
                pharmacist.setEmail("pharma@store.com");
                pharmacist.setFullName("Pharmacist User");
                pharmacist.setRole(User.UserRole.PHARMACIST);
                pharmacist = userRepository.save(pharmacist);
                pharmacistId = pharmacist.getId();
                pharmacistToken = "Bearer " + jwtTokenProvider.generateToken(pharmacistId, "pharmacist", "PHARMACIST");
        }

        @Test
        void fullDrugLifecycleFlow() throws Exception {
                // 1. Register Drug
                DrugRegistrationRequest drugReq = new DrugRegistrationRequest();
                drugReq.setName("Lifesaver");
                drugReq.setNdc("12345-678-90");
                drugReq.setManufacturer("PharmaCorp");
                drugReq.setManufacturerId(manufacturerId);
                drugReq.setDescription("A miracle drug");
                drugReq.setDosageForm("Tablet");
                drugReq.setStrength("500mg");

                MvcResult drugResult = mockMvc.perform(post("/api/v1/drugs")
                                .header("Authorization", manufacturerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(drugReq)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andReturn();

                DrugResponse drugResponse = objectMapper.readValue(drugResult.getResponse().getContentAsString(),
                                DrugResponse.class);
                Long drugId = drugResponse.getId();

                // 2. Approve Drug
                mockMvc.perform(put("/api/v1/drugs/" + drugId + "/approve")
                                .header("Authorization", regulatorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APPROVED"));

                // 3. Create Batch
                BatchCreationRequest batchReq = new BatchCreationRequest();
                batchReq.setBatchNumber("BATCH-001");
                batchReq.setDrugId(drugId);
                batchReq.setManufacturingDate(LocalDate.now());
                batchReq.setExpirationDate(LocalDate.now().plusYears(2));
                batchReq.setQuantity(100);

                MvcResult batchResult = mockMvc.perform(post("/api/v1/batches")
                                .header("Authorization", manufacturerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(batchReq)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("ACTIVE"))
                                .andReturn();

                BatchResponse batchResponse = objectMapper.readValue(batchResult.getResponse().getContentAsString(),
                                BatchResponse.class);
                Long batchId = batchResponse.getId();

                // 4. Serialize Units
                MvcResult unitsResult = mockMvc.perform(post("/api/v1/units/bulk")
                                .header("Authorization", manufacturerToken)
                                .param("batchId", batchId.toString())
                                .param("gtin", "GTIN-123456")
                                .param("quantity", "5"))
                                .andExpect(status().isCreated())
                                .andReturn();

                SerializedUnitResponse[] units = objectMapper.readValue(unitsResult.getResponse().getContentAsString(),
                                SerializedUnitResponse[].class);

                // Verify we created 5 units
                assertEquals(5, units.length);
                assertNotNull(units[0].getSerialNumber());
                assertNotNull(units[0].getCryptoTail());
        }
}

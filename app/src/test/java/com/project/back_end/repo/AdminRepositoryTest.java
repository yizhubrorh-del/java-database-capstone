package com.project.back_end.repo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.project.back_end.TestData;
import com.project.back_end.models.Admin;

@DataJpaTest
class AdminRepositoryTest {

    @Autowired private AdminRepository adminRepository;

    @Test
    void findByUsernameReturnsSavedAdmin() {
        Admin admin = TestData.admin();
        admin.setId(null);
        adminRepository.save(admin);

        Admin found = adminRepository.findByUsername("admin");

        assertThat(found).isNotNull();
        assertThat(found.getPassword()).isEqualTo("admin123");
    }
}

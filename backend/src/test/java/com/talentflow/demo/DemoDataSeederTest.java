package com.talentflow.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.talentflow.TestcontainersConfiguration;

@SpringBootTest(properties = "talentflow.demo.seed=true")
@Import(TestcontainersConfiguration.class)
class DemoDataSeederTest {

    @Autowired
    private DemoDataSeeder seeder;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void seedsACompleteDemoDatasetOnce() {
        assertThat(count("SELECT count(*) FROM job")).isEqualTo(DemoData.JOBS.size());
        assertThat(count("SELECT count(*) FROM job_application")).isEqualTo(DemoData.APPLICATIONS.size());
        assertThat(count("SELECT count(DISTINCT stage) FROM job_application")).isEqualTo(6);
        assertThat(count("SELECT count(*) FROM job_application WHERE ai_status = 'DONE'")).isEqualTo(DemoData.APPLICATIONS.size());
        assertThat(count("SELECT count(*) FROM candidate WHERE keycloak_user_id = '" + DemoData.CANDIDATE_USER + "'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM interview WHERE '" + DemoData.MANAGER_USER + "' = ANY (interviewer_ids)")).isPositive();
        assertThat(count("SELECT count(*) FROM feedback")).isEqualTo(3);
        assertThat(count("SELECT count(*) FROM consent")).isEqualTo(DemoData.APPLICATIONS.size());
        assertThat(jdbc.queryForObject("SELECT ai_summary FROM job_application a JOIN candidate c ON c.id = a.candidate_id"
                + " WHERE c.full_name = 'Lena Schmidt'", String.class)).doesNotContain("Lena");

        seeder.run(new DefaultApplicationArguments());

        assertThat(count("SELECT count(*) FROM job")).isEqualTo(DemoData.JOBS.size());
    }

    private int count(String sql) {
        return jdbc.queryForObject(sql, Integer.class);
    }
}

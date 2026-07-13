package com.samjay.bloom_filter_project.config;

import com.samjay.bloom_filter_project.model.AppUser;
import com.samjay.bloom_filter_project.repository.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("NullableProblems")
@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository repository;

    public DataSeeder(AppUserRepository repository) {

        this.repository = repository;

    }

    @Override
    public void run(String... args) {

        if (repository.count() == 0) {

            List<AppUser> seedUsers = List.of(
                    new AppUser("Samuel", "Adeyemi", "samuel_dev"),
                    new AppUser("Jane", "Doe", "jane_doe"),
                    new AppUser("Michael", "Okafor", "mike99"),
                    new AppUser("Chinwe", "Eze", "chinwe_e"),
                    new AppUser("Tunde", "Bakare", "tunde_b"),
                    new AppUser("Ngozi", "Umeh", "ngozi_umeh"),
                    new AppUser("David", "Johnson", "david_j"),
                    new AppUser("Amaka", "Nwosu", "amaka_n"),
                    new AppUser("Emeka", "Obi", "emeka_obi"),
                    new AppUser("Grace", "Ibe", "grace_ibe"),
                    new AppUser("Femi", "Alade", "femi_alade"),
                    new AppUser("Blessing", "Chukwu", "blessing_c"),
                    new AppUser("Kunle", "Ade", "kunle_ade"),
                    new AppUser("Sarah", "Musa", "sarah_musa"),
                    new AppUser("Ifeanyi", "Ude", "ifeanyi_ude"),
                    new AppUser("Peace", "Etim", "peace_etim"),
                    new AppUser("Bayo", "Salami", "bayo_salami"),
                    new AppUser("Chidera", "Nnaji", "chidera_n"),
                    new AppUser("Victor", "Ojo", "victor_ojo"),
                    new AppUser("Aisha", "Bello", "aisha_bello"));

            repository.saveAll(seedUsers);

            log.info("Seeded {} usernames into the database.", seedUsers.size());

        }
    }
}
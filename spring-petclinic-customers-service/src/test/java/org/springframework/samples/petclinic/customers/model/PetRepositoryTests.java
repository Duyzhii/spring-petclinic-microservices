package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PetRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PetRepository petRepository;

    @Test
    void testFindById() {
        // Create test data
        PetType type = new PetType();
        type.setName("Dog");
        entityManager.persist(type);

        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("1234567890");
        entityManager.persist(owner);

        Pet pet = new Pet();
        pet.setName("Max");
        pet.setBirthDate(new Date());
        pet.setType(type);
        pet.setOwner(owner);
        entityManager.persist(pet);
        entityManager.flush();

        // Test findById
        Pet found = petRepository.findById(pet.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Max", found.getName());
        assertEquals("Dog", found.getType().getName());
        assertEquals("John", found.getOwner().getFirstName());
    }

    @Test
    void testSave() {
        // Create and save a new pet
        PetType type = new PetType();
        type.setName("Cat");
        entityManager.persist(type);

        Owner owner = new Owner();
        owner.setFirstName("Jane");
        owner.setLastName("Smith");
        owner.setAddress("456 Oak St");
        owner.setCity("Boston");
        owner.setTelephone("0987654321");
        entityManager.persist(owner);

        Pet pet = new Pet();
        pet.setName("Whiskers");
        pet.setBirthDate(new Date());
        pet.setType(type);
        pet.setOwner(owner);

        Pet saved = petRepository.save(pet);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("Whiskers", saved.getName());
        assertEquals("Cat", saved.getType().getName());
    }
} 
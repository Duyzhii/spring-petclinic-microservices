package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OwnerRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    void testFindById() {
        // Create test data
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Smith");
        owner.setAddress("789 Pine St");
        owner.setCity("Chicago");
        owner.setTelephone("5555555555");
        entityManager.persist(owner);
        entityManager.flush();

        // Test findById
        Owner found = ownerRepository.findById(owner.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("John", found.getFirstName());
        assertEquals("Smith", found.getLastName());
        assertEquals("Chicago", found.getCity());
    }

    @Test
    void testSave() {
        // Create and save a new owner
        Owner owner = new Owner();
        owner.setFirstName("Alice");
        owner.setLastName("Johnson");
        owner.setAddress("321 Elm St");
        owner.setCity("Dallas");
        owner.setTelephone("1112223333");

        Owner saved = ownerRepository.save(owner);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("Alice", saved.getFirstName());
        assertEquals("Johnson", saved.getLastName());
        assertEquals("Dallas", saved.getCity());
    }
} 
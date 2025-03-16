package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OwnerRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    void testFindByLastName() {
        // Create test data
        Owner owner1 = new Owner();
        owner1.setFirstName("John");
        owner1.setLastName("Doe");
        owner1.setAddress("123 Main St");
        owner1.setCity("Springfield");
        owner1.setTelephone("1234567890");
        entityManager.persist(owner1);

        Owner owner2 = new Owner();
        owner2.setFirstName("Jane");
        owner2.setLastName("Doe");
        owner2.setAddress("456 Oak St");
        owner2.setCity("Boston");
        owner2.setTelephone("0987654321");
        entityManager.persist(owner2);

        entityManager.flush();

        // Test findByLastName
        List<Owner> owners = ownerRepository.findByLastName("Doe");
        assertEquals(2, owners.size());
        assertTrue(owners.stream().allMatch(o -> o.getLastName().equals("Doe")));
    }

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
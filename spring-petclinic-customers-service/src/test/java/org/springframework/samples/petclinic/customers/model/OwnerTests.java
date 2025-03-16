package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

class OwnerTests {

    @Test
    void testOwnerProperties() {
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("1234567890");

        assertEquals(1, owner.getId());
        assertEquals("John", owner.getFirstName());
        assertEquals("Doe", owner.getLastName());
        assertEquals("123 Main St", owner.getAddress());
        assertEquals("Springfield", owner.getCity());
        assertEquals("1234567890", owner.getTelephone());
    }

    @Test
    void testPetManagement() {
        Owner owner = new Owner();
        Pet pet1 = new Pet();
        pet1.setName("Max");
        Pet pet2 = new Pet();
        pet2.setName("Buddy");

        owner.addPet(pet1);
        owner.addPet(pet2);

        Set<Pet> pets = owner.getPets();
        assertEquals(2, pets.size());
        assertTrue(pets.stream().anyMatch(p -> p.getName().equals("Max")));
        assertTrue(pets.stream().anyMatch(p -> p.getName().equals("Buddy")));
    }

    @Test
    void testGetPet() {
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setName("Max");
        owner.addPet(pet);

        // Test case-insensitive pet name search
        Pet foundPet = owner.getPet("max", true);
        assertNotNull(foundPet);
        assertEquals("Max", foundPet.getName());

        // Test non-existent pet
        Pet notFound = owner.getPet("nonexistent", true);
        assertNull(notFound);
    }
} 
package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class OwnerTests {

    @Test
    void testOwnerProperties() {
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("1234567890");

        assertEquals("John", owner.getFirstName());
        assertEquals("Doe", owner.getLastName());
        assertEquals("123 Main St", owner.getAddress());
        assertEquals("Springfield", owner.getCity());
        assertEquals("1234567890", owner.getTelephone());
    }

    @Test
    void testToString() {
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("1234567890");

        String toString = owner.toString();
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
        assertTrue(toString.contains("123 Main St"));
        assertTrue(toString.contains("Springfield"));
        assertTrue(toString.contains("1234567890"));
    }

    @Test
    void testPetManagement() {
        Owner owner = new Owner();
        Pet pet1 = new Pet();
        pet1.setName("Max");
        Pet pet2 = new Pet();
        pet2.setName("Buddy");
        Pet pet3 = new Pet();
        pet3.setName("Bella");

        owner.addPet(pet1);
        owner.addPet(pet2);
        owner.addPet(pet3);

        List<Pet> pets = owner.getPets();
        assertEquals(3, pets.size());
        // Verify pets are sorted by name
        assertEquals("Bella", pets.get(0).getName());
        assertEquals("Buddy", pets.get(1).getName());
        assertEquals("Max", pets.get(2).getName());
    }

    @Test
    void testEmptyPetList() {
        Owner owner = new Owner();
        List<Pet> pets = owner.getPets();
        assertNotNull(pets);
        assertTrue(pets.isEmpty());
    }

    @Test
    void testGetPetVariousCases() {
        Owner owner = new Owner();
        
        // Test with empty pet list
        assertNull(owner.getPet("nonexistent", true));

        // Add a pet and test various cases
        Pet pet = new Pet();
        pet.setName("Max");
        owner.addPet(pet);

        // Test exact match
        Pet foundPet = owner.getPet("Max", true);
        assertNotNull(foundPet);
        assertEquals("Max", foundPet.getName());

        // Test case-insensitive match
        foundPet = owner.getPet("max", true);
        assertNotNull(foundPet);
        assertEquals("Max", foundPet.getName());

        foundPet = owner.getPet("MAX", true);
        assertNotNull(foundPet);
        assertEquals("Max", foundPet.getName());

        // Test non-existent pet
        Pet notFound = owner.getPet("nonexistent", true);
        assertNull(notFound);

        // Test with null name
        Pet petWithNullName = new Pet();
        owner.addPet(petWithNullName);
        assertNull(owner.getPet("null", true));
    }

    @Test
    void testPetOwnerReference() {
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setName("Max");
        
        owner.addPet(pet);
        
        // Verify the bi-directional relationship
        assertEquals(owner, pet.getOwner());
        assertTrue(owner.getPets().contains(pet));
    }
} 
package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class PetTests {

    @Test
    void testPetProperties() {
        Pet pet = new Pet();
        pet.setName("Max");
        
        Date birthDate = new Date();
        pet.setBirthDate(birthDate);

        PetType type = new PetType();
        type.setName("Dog");
        pet.setType(type);

        Owner owner = new Owner();
        owner.setFirstName("John");
        pet.setOwner(owner);

        assertEquals("Max", pet.getName());
        assertEquals(birthDate, pet.getBirthDate());
        assertEquals("Dog", pet.getType().getName());
        assertEquals("John", pet.getOwner().getFirstName());
    }

    @Test
    void testToString() {
        Pet pet = new Pet();
        pet.setName("Max");
        
        Date birthDate = new Date();
        pet.setBirthDate(birthDate);

        PetType type = new PetType();
        type.setName("Dog");
        pet.setType(type);

        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        pet.setOwner(owner);

        String toString = pet.toString();
        assertTrue(toString.contains("Max"));
        assertTrue(toString.contains("Dog"));
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
    }

    @Test
    void testEquals() {
        Pet pet1 = new Pet();
        pet1.setName("Max");
        Date birthDate = new Date();
        pet1.setBirthDate(birthDate);
        PetType type = new PetType();
        type.setName("Dog");
        pet1.setType(type);

        Pet pet2 = new Pet();
        pet2.setName("Max");
        pet2.setBirthDate(birthDate);
        pet2.setType(type);

        // Test equality
        assertTrue(pet1.equals(pet1)); // Same object
        assertTrue(pet1.equals(pet2)); // Equal objects
        assertFalse(pet1.equals(null)); // Null comparison
        assertFalse(pet1.equals(new Object())); // Different types

        // Test hashCode
        assertEquals(pet1.hashCode(), pet2.hashCode());
    }

    @Test
    void testPetType() {
        PetType type = new PetType();
        type.setName("Cat");
        assertEquals("Cat", type.getName());
    }

    @Test
    void testOwnerReference() {
        Pet pet = new Pet();
        Owner owner = new Owner();
        owner.setFirstName("Jane");
        owner.setLastName("Doe");
        
        pet.setOwner(owner);
        
        assertNotNull(pet.getOwner());
        assertEquals("Jane", pet.getOwner().getFirstName());
        assertEquals("Doe", pet.getOwner().getLastName());
    }

    @Test
    void testNullProperties() {
        Pet pet = new Pet();
        assertNull(pet.getName());
        assertNull(pet.getBirthDate());
        assertNull(pet.getType());
        assertNull(pet.getOwner());
        assertNull(pet.getId());
    }
} 
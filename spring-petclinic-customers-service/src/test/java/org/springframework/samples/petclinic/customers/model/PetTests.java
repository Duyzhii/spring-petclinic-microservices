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
} 
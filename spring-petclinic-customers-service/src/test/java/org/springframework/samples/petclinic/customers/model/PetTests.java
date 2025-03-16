package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PetTests {

    @Test
    void testPetProperties() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Max");
        
        LocalDate birthDate = LocalDate.now().minusYears(2);
        pet.setBirthDate(birthDate);

        PetType type = new PetType();
        type.setId(1);
        type.setName("Dog");
        pet.setType(type);

        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        pet.setOwner(owner);

        assertEquals(1, pet.getId());
        assertEquals("Max", pet.getName());
        assertEquals(birthDate, pet.getBirthDate());
        assertEquals("Dog", pet.getType().getName());
        assertEquals("John", pet.getOwner().getFirstName());
    }

    @Test
    void testPetType() {
        PetType type = new PetType();
        type.setId(1);
        type.setName("Cat");

        assertEquals(1, type.getId());
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
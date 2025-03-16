package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetDetailsTest {

    @Test
    void testCreatePetDetails() {
        PetDetails petDetails = new PetDetails();
        petDetails.setId(1);
        petDetails.setName("Max");
        petDetails.setBirthDate(new Date());
        petDetails.setTypeId(2);
        petDetails.setOwnerId(3);

        assertEquals(1, petDetails.getId());
        assertEquals("Max", petDetails.getName());
        assertNotNull(petDetails.getBirthDate());
        assertEquals(2, petDetails.getTypeId());
        assertEquals(3, petDetails.getOwnerId());
    }

    @Test
    void testCreateFromPet() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Max");
        Date birthDate = new Date();
        pet.setBirthDate(birthDate);

        PetType type = new PetType();
        type.setId(2);
        pet.setType(type);

        PetDetails petDetails = new PetDetails(pet);

        assertEquals(1, petDetails.getId());
        assertEquals("Max", petDetails.getName());
        assertEquals(birthDate, petDetails.getBirthDate());
        assertEquals(2, petDetails.getTypeId());
    }
} 
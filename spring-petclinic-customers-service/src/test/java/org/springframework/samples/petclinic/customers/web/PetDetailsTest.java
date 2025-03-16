package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetDetailsTest {

    @Test
    void testCreatePetDetails() {
        Date birthDate = new Date();
        PetType type = new PetType();
        type.setName("Dog");

        PetDetails petDetails = new PetDetails(1L, "Max", "John Doe", birthDate, type);

        assertEquals(1L, petDetails.id());
        assertEquals("Max", petDetails.name());
        assertEquals("John Doe", petDetails.owner());
        assertEquals(birthDate, petDetails.birthDate());
        assertEquals(type, petDetails.type());
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
        type.setName("Dog");
        pet.setType(type);

        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        pet.setOwner(owner);

        PetDetails petDetails = new PetDetails(pet);

        assertEquals(1L, petDetails.id());
        assertEquals("Max", petDetails.name());
        assertEquals("John Doe", petDetails.owner());
        assertEquals(birthDate, petDetails.birthDate());
        assertEquals(type, petDetails.type());
    }
} 
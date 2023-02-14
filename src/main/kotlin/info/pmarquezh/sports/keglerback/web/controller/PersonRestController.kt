package info.pmarquezh.sports.keglerback.web.controller

import info.pmarquezh.sports.keglerback.model.person.Person
import info.pmarquezh.sports.keglerback.model.person.PersonDto
import java.io.File
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@RequestMapping("/kinesikAPI/1.0/persons")
@RestController
class PersonRestController {

    /**
     * C
     */
    @PostMapping("")
    fun persistPerson(@RequestBody person: PersonDto): ResponseEntity<Unit> {
        return ResponseEntity(HttpStatus.CREATED)
    }

    /**
     * RR
     */
    @GetMapping(value=[""])
    fun retrievePersons(): ResponseEntity<String> =
        ResponseEntity.ok().body(File("mock_persons.json").readText(Charsets.UTF_8))

    /**
     * R
     */
    @GetMapping(value=["/{personId}"])
    fun retrievePerson(@PathVariable("personId") personId: String): ResponseEntity<String> =
        ResponseEntity.ok().body(File("mock_person.json").readText(Charsets.UTF_8))

    /**
     * U
     */
    @PutMapping("/{personId}")
    fun updatePerson(@PathVariable("personId") personId: String, @RequestBody person: Person): ResponseEntity<Unit> {
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /**
     * D
     */
    @DeleteMapping("/{personId}")
    fun deletePerson(@PathVariable("personId") personId: String): ResponseEntity<Unit> {
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}

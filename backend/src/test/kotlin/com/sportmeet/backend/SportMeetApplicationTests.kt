package com.sportmeet.backend

import com.sportmeet.backend.dto.LoginRequest
import com.sportmeet.backend.dto.RegisterRequest
import com.sportmeet.backend.service.AuthService
import com.sportmeet.backend.service.TournamentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class SportMeetApplicationTests {

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var tournamentService: TournamentService

    @Autowired
    private lateinit var tournamentRepository: com.sportmeet.backend.repository.TournamentRepository

    @Test
    fun contextLoads() {
        // Verifies Spring context, JPA repositories, and security beans load cleanly
    }

    @Test
    fun testAuthenticationFlow() {
        // Test Admin and Organizer logins seeded by DataInitializer
        val adminAuth = authService.login(LoginRequest("admin@sportmeet.com", "admin123"))
        assertNotNull(adminAuth.token)
        assertEquals("ADMIN", adminAuth.role)

        val orgAuth = authService.login(LoginRequest("organizer@sportmeet.com", "pass123"))
        assertNotNull(orgAuth.token)
        assertEquals("ORGANIZER", orgAuth.role)

        // Test dynamic Player Registration
        val newPlayer = authService.register(
            RegisterRequest(
                name = "Test Athlete",
                email = "athlete_${System.currentTimeMillis()}@sportmeet.com",
                phone = "+91 99999 11111",
                password = "password123",
                role = "PLAYER"
            )
        )
        assertNotNull(newPlayer.token)
        assertEquals("PLAYER", newPlayer.role)
    }

    @Test
    fun testTournamentSearch() {
        val orgAuth = authService.login(LoginRequest("organizer@sportmeet.com", "pass123"))
        val created = tournamentService.createTournament(
            organizerId = orgAuth.userId,
            request = com.sportmeet.backend.dto.CreateTournamentRequest(
                name = "Erode Cricket Trophy",
                sportId = "cricket",
                location = "Erode",
                venueName = "VOC Park Ground",
                groundName = "Pitch A",
                address = "Erode Central",
                date = "25 Nov 2026",
                startTime = "08:30 AM",
                contactNumber = "9876543210"
            )
        )
        val tour = tournamentRepository.findById(created.id).get()
        tour.status = "APPROVED"
        tournamentRepository.save(tour)

        // Test search by sport
        val cricketResults = tournamentService.searchTournaments("cricket", null, null, 0, 10)
        assertTrue(cricketResults.tournaments.isNotEmpty())
        assertTrue(cricketResults.tournaments.any { it.sportId == "cricket" })

        // Test search by location
        val erodeResults = tournamentService.searchTournaments(null, "Erode", null, 0, 10)
        assertTrue(erodeResults.tournaments.isNotEmpty())
        assertTrue(erodeResults.tournaments.all { it.location.equals("Erode", ignoreCase = true) })
    }

    @Test
    fun testGroundLocationAndCoordinates() {
        val orgAuth = authService.login(LoginRequest("organizer@sportmeet.com", "pass123"))

        val created = tournamentService.createTournament(
            organizerId = orgAuth.userId,
            request = com.sportmeet.backend.dto.CreateTournamentRequest(
                name = "KEC Premier League",
                sportId = "cricket",
                location = "Erode",
                venueName = "Kongu Engineering College",
                groundName = "Main Cricket Ground",
                address = "Perundurai, Erode",
                date = "25 Nov 2026",
                startTime = "08:30 AM",
                contactNumber = "9876543210",
                latitude = 11.274215,
                longitude = 77.607024
            )
        )

        assertNotNull(created)
        assertEquals("Main Cricket Ground", created.groundName)
        assertEquals(11.274215, created.latitude)
        assertEquals(77.607024, created.longitude)

        // Test coordinate validation
        org.junit.jupiter.api.assertThrows<org.springframework.web.server.ResponseStatusException> {
            tournamentService.createTournament(
                organizerId = orgAuth.userId,
                request = com.sportmeet.backend.dto.CreateTournamentRequest(
                    name = "Invalid Coord Cup",
                    sportId = "cricket",
                    location = "Erode",
                    venueName = "Test Ground",
                    date = "25 Nov 2026",
                    startTime = "08:30 AM",
                    contactNumber = "9876543210",
                    latitude = 95.0, // Invalid latitude > 90
                    longitude = 77.0
                )
            )
        }
    }
}

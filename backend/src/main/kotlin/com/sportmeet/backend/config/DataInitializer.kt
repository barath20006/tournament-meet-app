package com.sportmeet.backend.config

import com.sportmeet.backend.entity.Registration
import com.sportmeet.backend.entity.Sport
import com.sportmeet.backend.entity.Tournament
import com.sportmeet.backend.entity.User
import com.sportmeet.backend.repository.RegistrationRepository
import com.sportmeet.backend.repository.SportRepository
import com.sportmeet.backend.repository.TournamentRepository
import com.sportmeet.backend.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val sportRepository: SportRepository,
    private val tournamentRepository: TournamentRepository,
    private val registrationRepository: RegistrationRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        // 1. Seed Sports
        if (sportRepository.count() == 0L) {
            val sports = listOf(
                Sport("cricket", "Cricket", "🏏", true),
                Sport("football", "Football", "⚽", true),
                Sport("volleyball", "Volleyball", "🏐", true),
                Sport("badminton", "Badminton", "🏸", true),
                Sport("basketball", "Basketball", "🏀", true),
                Sport("table_tennis", "Table Tennis", "🏓", true),
                Sport("kabaddi", "Kabaddi", "🤼", true),
                Sport("tennis", "Tennis", "🎾", false),
                Sport("hockey", "Hockey", "🏑", false),
                Sport("athletics", "Athletics", "🏃", false)
            )
            sportRepository.saveAll(sports)
        }

        // 2. Seed Default Users
        val admin = userRepository.findByEmail("admin@sportmeet.com").orElseGet {
            userRepository.save(
                User(
                    id = "usr_admin_001",
                    name = "SportMeet Admin",
                    email = "admin@sportmeet.com",
                    phone = "+91 90000 00001",
                    passwordHash = passwordEncoder.encode("admin123"),
                    role = "ADMIN"
                )
            )
        }

        val organizer = userRepository.findByEmail("organizer@sportmeet.com").orElseGet {
            userRepository.save(
                User(
                    id = "usr_org_001",
                    name = "Karthik Raja",
                    email = "organizer@sportmeet.com",
                    phone = "+91 98765 43210",
                    passwordHash = passwordEncoder.encode("pass123"),
                    role = "ORGANIZER"
                )
            )
        }

        val player = userRepository.findByEmail("player@sportmeet.com").orElseGet {
            userRepository.save(
                User(
                    id = "usr_player_001",
                    name = "Arun Kumar",
                    email = "player@sportmeet.com",
                    phone = "+91 98420 12345",
                    passwordHash = passwordEncoder.encode("pass123"),
                    role = "PLAYER"
                )
            )
        }

        // 3. Seed Sample Tournaments
        if (tournamentRepository.count() == 0L) {
            val cricketSport = sportRepository.findById("cricket").orElseThrow()
            val volleyballSport = sportRepository.findById("volleyball").orElseThrow()
            val footballSport = sportRepository.findById("football").orElseThrow()
            val basketballSport = sportRepository.findById("basketball").orElseThrow()

            val tour1 = tournamentRepository.save(
                Tournament(
                    id = "tour_001",
                    name = "Erode Cricket Cup 2026",
                    sport = cricketSport,
                    tournamentType = "T20 Knockout",
                    location = "Erode",
                    venueName = "VOC Park Sports Complex",
                    address = "Park Road, Erode, Tamil Nadu",
                    date = "15 October 2026",
                    startTime = "06:00 PM",
                    endTime = "10:30 PM",
                    registrationDeadline = "13 October 2026",
                    entryFee = 2000.0,
                    prizeMoney = 25000.0,
                    maxTeams = 16,
                    registeredTeams = 12,
                    playersPerTeam = 11,
                    ageCategory = "Open",
                    organizer = organizer,
                    organizerName = organizer.name,
                    contactNumber = organizer.phone,
                    whatsappNumber = organizer.phone,
                    description = "Annual premier leather & tennis ball T20 tournament. Floodlight matches with digital scoreboards.",
                    status = "APPROVED",
                    latitude = 11.3410,
                    longitude = 77.7172
                )
            )

            tournamentRepository.save(
                Tournament(
                    id = "tour_002",
                    name = "Erode Volleyball Championship",
                    sport = volleyballSport,
                    tournamentType = "League + Knockout",
                    location = "Erode",
                    venueName = "District Indoor Stadium",
                    address = "Sampath Nagar, Erode",
                    date = "18 October 2026",
                    startTime = "09:00 AM",
                    endTime = "06:00 PM",
                    registrationDeadline = "16 October 2026",
                    entryFee = 1500.0,
                    prizeMoney = 15000.0,
                    maxTeams = 20,
                    registeredTeams = 15,
                    playersPerTeam = 6,
                    ageCategory = "Open",
                    organizer = organizer,
                    organizerName = organizer.name,
                    contactNumber = organizer.phone,
                    whatsappNumber = organizer.phone,
                    description = "District-level championship for club and college teams. Cash prizes and trophies for Top 4.",
                    status = "APPROVED",
                    latitude = 11.3425,
                    longitude = 77.7280
                )
            )

            tournamentRepository.save(
                Tournament(
                    id = "tour_003",
                    name = "Salem Football League",
                    sport = footballSport,
                    tournamentType = "7s Knockout",
                    location = "Salem",
                    venueName = "Gandhi Stadium Ground",
                    address = "Cherry Road, Salem",
                    date = "20 October 2026",
                    startTime = "07:00 AM",
                    endTime = "07:00 PM",
                    registrationDeadline = "18 October 2026",
                    entryFee = 3000.0,
                    prizeMoney = 35000.0,
                    maxTeams = 24,
                    registeredTeams = 18,
                    playersPerTeam = 7,
                    ageCategory = "Open",
                    organizer = organizer,
                    organizerName = organizer.name,
                    contactNumber = organizer.phone,
                    whatsappNumber = organizer.phone,
                    description = "State-wide 7-a-side football tournament. FIFA standard turf with live streaming.",
                    status = "APPROVED",
                    latitude = 11.6643,
                    longitude = 78.1460
                )
            )

            // Seed a PENDING_APPROVAL tournament for Admin testing
            tournamentRepository.save(
                Tournament(
                    id = "tour_004",
                    name = "Kongu Summer Basketball Bash",
                    sport = basketballSport,
                    tournamentType = "3x3 Half Court",
                    location = "Erode",
                    venueName = "Kongu Arts Court",
                    address = "Perundurai, Erode",
                    date = "05 November 2026",
                    startTime = "03:00 PM",
                    endTime = "09:00 PM",
                    registrationDeadline = "02 November 2026",
                    entryFee = 800.0,
                    prizeMoney = 10000.0,
                    maxTeams = 16,
                    registeredTeams = 4,
                    playersPerTeam = 4,
                    ageCategory = "Under 21",
                    organizer = organizer,
                    organizerName = organizer.name,
                    contactNumber = organizer.phone,
                    whatsappNumber = organizer.phone,
                    description = "Fast-paced 3v3 basketball tournament. Waiting for Admin approval.",
                    status = "PENDING_APPROVAL",
                    latitude = 11.2721,
                    longitude = 77.6083
                )
            )

            // Seed Registration for Player
            registrationRepository.save(
                Registration(
                    id = "reg_001",
                    tournament = tour1,
                    user = player,
                    teamName = "Erode Strikers XI",
                    captainName = player.name,
                    phone = player.phone,
                    email = player.email,
                    numberOfPlayers = 11,
                    notes = "Leather ball squad ready for evening knockout.",
                    status = "APPROVED"
                )
            )
        }
    }
}

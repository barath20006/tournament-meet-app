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

        // Clean slate: Tournaments and squad registrations start empty.
        // Users can create and register for new tournaments directly in the app.
    }
}

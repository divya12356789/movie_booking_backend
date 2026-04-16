package com.movieBooking.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.movieBooking.dto.CreateMovieShowRequest;
import com.movieBooking.dto.CreateScreenSeatRequest;
import com.movieBooking.model.City;
import com.movieBooking.model.Movie;
import com.movieBooking.model.Screen;
import com.movieBooking.model.Theatre;
import com.movieBooking.repository.CityRepository;
import com.movieBooking.repository.MovieRepository;
import com.movieBooking.repository.MovieShowRepository;
import com.movieBooking.repository.ScreenRepository;
import com.movieBooking.repository.ScreenSeatRepository;
import com.movieBooking.repository.TheatreRepository;
import com.movieBooking.service.MovieShowService;
import com.movieBooking.service.ScreenService;

@Component
public class DemoDataSeeder implements ApplicationRunner {

    private final CityRepository cityRepository;
    private final TheatreRepository theatreRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final ScreenSeatRepository screenSeatRepository;
    private final MovieShowRepository movieShowRepository;
    private final ScreenService screenService;
    private final MovieShowService movieShowService;

    public DemoDataSeeder(
            CityRepository cityRepository,
            TheatreRepository theatreRepository,
            MovieRepository movieRepository,
            ScreenRepository screenRepository,
            ScreenSeatRepository screenSeatRepository,
            MovieShowRepository movieShowRepository,
            ScreenService screenService,
            MovieShowService movieShowService) {
        this.cityRepository = cityRepository;
        this.theatreRepository = theatreRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.screenSeatRepository = screenSeatRepository;
        this.movieShowRepository = movieShowRepository;
        this.screenService = screenService;
        this.movieShowService = movieShowService;
    }

    @Override
    public void run(ApplicationArguments args) {
        City mumbai = ensureCity("Mumbai", "Maharashtra");
        City bengaluru = ensureCity("Bengaluru", "Karnataka");

        Movie skylineRescue = ensureMovie("Skyline Rescue", "Action", 132);
        Movie velvetHours = ensureMovie("Velvet Hours", "Romance", 118);
        Movie orbitEleven = ensureMovie("Orbit Eleven", "Sci-Fi", 146);
        Movie courtroomEcho = ensureMovie("Courtroom Echo", "Drama", 124);

        Theatre auroraCineplex = ensureTheatre(mumbai, "Aurora Cineplex", "Marine Drive, Mumbai");
        Theatre neonScreens = ensureTheatre(bengaluru, "Neon Screens", "MG Road, Bengaluru");

        Screen auroraScreen = ensureScreen(auroraCineplex, "Screen 1", 12);
        Screen neonScreen = ensureScreen(neonScreens, "Screen 1", 12);

        ensureScreenLayout(auroraScreen);
        ensureScreenLayout(neonScreen);

        if (movieShowRepository.count() == 0) {
            seedShow(skylineRescue, auroraScreen, LocalDateTime.now().plusHours(5), 320, "English");
            seedShow(velvetHours, auroraScreen, LocalDateTime.now().plusHours(8), 280, "Hindi");
            seedShow(orbitEleven, neonScreen, LocalDateTime.now().plusHours(6), 340, "English");
            seedShow(courtroomEcho, neonScreen, LocalDateTime.now().plusHours(9), 300, "Hindi");
        }
    }

    private City ensureCity(String name, String state) {
        return cityRepository.findAll().stream()
                .filter(city -> name.equalsIgnoreCase(city.getName())
                        && (state == null ? city.getState() == null : state.equalsIgnoreCase(city.getState())))
                .findFirst()
                .orElseGet(() -> {
                    City city = new City();
                    city.setName(name);
                    city.setState(state);
                    city.setActive(true);
                    return cityRepository.save(city);
                });
    }

    private Movie ensureMovie(String title, String genre, int duration) {
        return movieRepository.findAll().stream()
                .filter(movie -> title.equalsIgnoreCase(movie.getTitle()))
                .findFirst()
                .orElseGet(() -> {
                    Movie movie = new Movie();
                    movie.setTitle(title);
                    movie.setGenre(genre);
                    movie.setDuration(duration);
                    return movieRepository.save(movie);
                });
    }

    private Theatre ensureTheatre(City city, String name, String address) {
        return theatreRepository.findAll().stream()
                .filter(theatre -> theatre.getCity() != null
                        && theatre.getCity().getId() != null
                        && theatre.getCity().getId().equals(city.getId())
                        && name.equalsIgnoreCase(theatre.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Theatre theatre = new Theatre();
                    theatre.setCity(city);
                    theatre.setName(name);
                    theatre.setAddress(address);
                    theatre.setActive(true);
                    return theatreRepository.save(theatre);
                });
    }

    private Screen ensureScreen(Theatre theatre, String name, int totalSeats) {
        return screenRepository.findAll().stream()
                .filter(screen -> screen.getTheatre() != null
                        && screen.getTheatre().getId() != null
                        && screen.getTheatre().getId().equals(theatre.getId())
                        && name.equalsIgnoreCase(screen.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Screen screen = new Screen();
                    screen.setTheatre(theatre);
                    screen.setName(name);
                    screen.setTotalSeats(totalSeats);
                    screen.setActive(true);
                    return screenRepository.save(screen);
                });
    }

    private void ensureScreenLayout(Screen screen) {
        if (screenSeatRepository.existsByScreenId(screen.getId())) {
            return;
        }

        String[] seatNumbers = { "A1", "A2", "A3", "A4", "B1", "B2", "B3", "B4", "C1", "C2", "C3", "C4" };
        String[] seatTypes = { "REGULAR", "REGULAR", "PREMIUM", "PREMIUM", "REGULAR", "REGULAR", "PREMIUM", "PREMIUM", "REGULAR", "REGULAR", "PREMIUM", "PREMIUM" };

        for (int index = 0; index < seatNumbers.length; index++) {
            screenService.createScreenSeat(new CreateScreenSeatRequest(
                    screen.getId(),
                    seatNumbers[index],
                    seatTypes[index],
                    seatNumbers[index].substring(0, 1),
                    index + 1));
        }
    }

    private void seedShow(Movie movie, Screen screen, LocalDateTime showStart, int price, String language) {
        movieShowService.createMovieShow(new CreateMovieShowRequest(
                movie.getId(),
                screen.getId(),
                showStart,
                showStart.plusHours(2),
                language,
                "2D",
                BigDecimal.valueOf(price)));
    }
}
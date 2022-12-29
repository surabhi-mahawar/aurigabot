package com.aurigabot.controller.calendar_events;

import com.aurigabot.dto.CreateEventRequestDTO;
import com.aurigabot.service.calendar_events.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    @RequestMapping(value = "/events", method = RequestMethod.GET)
    public ResponseEntity<Mono<String>> fetchCalendarEvents(@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate) {
        Mono<String> response = calendarService.fetchCalendarEvents(startDate, endDate, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    @RequestMapping(value = "/events", method = RequestMethod.GET, params = "search")
//    public ResponseEntity<String> fetchCalendarEvents(@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate, @RequestParam(value = "search") String search) {
//        String response = calendarService.fetchCalendarEvents(startDate, endDate, search);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }


    @RequestMapping(value = "/createEvent", method = RequestMethod.POST)
    public ResponseEntity<String> createEvent(@RequestBody CreateEventRequestDTO createEventRequestDTO) {
        String response = calendarService.createGoogleCalendarEvent(createEventRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteEvent", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteEvent(@RequestParam(value = "title") String title) {
        String response = calendarService.deleteGoogleCalendarEvent(title);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}

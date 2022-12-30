package com.aurigabot.service.calendar_events;

import com.aurigabot.dto.CreateEventRequestDTO;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CalendarService {
    @Autowired
    private CalendarUserService calendarUserService;

    private static HttpTransport httpTransport;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "GoogleCalendar";
    private static com.google.api.services.calendar.Calendar client;

    /**
     * Fetch google calendar events from caledar API
     * @param startDate
     * @param endDate
     * @param search
     * @return
     */
    public Mono<String> fetchCalendarEvents(String startDate, String endDate, String email, String search) {
            return calendarUserService.getCurrentUser(email).map(user->{
                Events eventList=null;
                String response="";
                GoogleCredential credential = new GoogleCredential().setAccessToken(user.getAccessToken());

                final DateTime startDateTime = new DateTime(startDate + "T00:00:00");
                final DateTime endDateTime = new DateTime(endDate + "T23:59:59");

                try {
                    httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                    client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                            .setApplicationName(APPLICATION_NAME).build();

                    Calendar.Events events = client.events();
                    if (search == null || search.trim().equals("")) {
                        eventList= events.list("primary").setTimeZone("Asia/Kolkata").setTimeMin(startDateTime).setTimeMax(endDateTime).execute();
                    } else {
                        eventList= events.list("primary").setTimeZone("Asia/Kolkata").setTimeMin(startDateTime).setTimeMax(endDateTime).setQ(search).execute();
                    }
                } catch (Exception e) {
                    response="Error in fetching google calendar data, please try again!";
                }

                List<Event> eventsList= eventList.getItems();
                if (eventsList.size() == 0) {
                    response += "There are no events for these dates";
                } else {
                    response += "Please find the list of events below:";
                    for(Event event: eventsList) {
                        JSONParser jsonParser = new JSONParser();
                        Object obj;
                        try {
                            obj = jsonParser.parse(event.get("start").toString());
                            JSONObject eventJson= (JSONObject) obj;
                            String date=eventJson.get("dateTime").toString();
                            OffsetDateTime odt = OffsetDateTime.parse(date);
                            response += "\n"+odt.toLocalDate()+" -> "+event.get("summary");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                response+="\n\nPlease select a option from the list to proceed further.";

                return response;
            });
    }

    /**
     * Create a Google Calendar event using calendar API
     * @param createEventRequestDTO
     * @return
     */
    public String createGoogleCalendarEvent(CreateEventRequestDTO createEventRequestDTO) {
        try {
//            String token = calendarUserService.getCurrentUser().block().getAccessToken();
            AtomicReference<String> token = new AtomicReference<>("");
            calendarUserService.getCurrentUser().subscribe(user->{
                token.set(user.getAccessToken());});
            GoogleCredential credential = new GoogleCredential().setAccessToken(token.get());
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();

            Event event = new Event()
                    .setSummary(createEventRequestDTO.getSummary())
                    .setLocation(createEventRequestDTO.getLocation())
                    .setDescription(createEventRequestDTO.getDescription());

            DateTime startDateTime = new DateTime(createEventRequestDTO.getStartDate());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(createEventRequestDTO.getTimezone());
            event.setStart(start);

            DateTime endDateTime = new DateTime(createEventRequestDTO.getEndDate());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(createEventRequestDTO.getTimezone());
            event.setEnd(end);

            event.setLocked(true);

            EventAttendee[] attendees = new EventAttendee[] {
                    new EventAttendee().setEmail("vishal.bothra@aurigait.com"),
//                    new EventAttendee().setEmail("sbrin@example.com"),
            };
            event.setAttendees(Arrays.asList(attendees));

            EventReminder[] reminderOverrides = new EventReminder[] {
                    new EventReminder().setMethod("email").setMinutes(24 * 60),
                    new EventReminder().setMethod("popup").setMinutes(10),
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);

            event.setVisibility("default");

            String calendarId = "primary";

            event = client.events().insert(calendarId, event).execute();
            return event.getHtmlLink();
        } catch (Exception e) {
            return "Error encountered while creating event: " + e.getMessage();
        }
    }

    public String deleteGoogleCalendarEvent(String title){
        try{
            AtomicReference<String> token = new AtomicReference<>("");
            calendarUserService.getCurrentUser().subscribe(user->{
                token.set(user.getAccessToken());});
            GoogleCredential credential = new GoogleCredential().setAccessToken(token.get());
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();

            String calendarId = "primary";
            String eventId="o5hl4mre7egu0ipjfaf71oure8";

            client.events().delete(calendarId,eventId).execute();

        }catch (Exception e){
            return "Error encountered while deleting event: " + e.getMessage();
        }

        return "Event Deleted";
    }
}

// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    Collection<TimeRange> blockedTimes = new ArrayList<TimeRange>();

    // Get important request information
    Collection<String> attendees = request.getAttendees();
    long duration = request.getDuration();

    // Base case: No attendees:
    if (attendees.size() == 0){

    }

    // For all events in event
    for (Event e : events){

      // Check if at least one request attendee is an event attendee, otherwise skip event
      ArrayList<String> eventAttendees = new ArrayList<String>(e.getAttendees());
      eventAttendees.retainAll(attendees);
      if (eventAttendees.size() != 0){

        // Insert the time to list of blockTimes
        eventTime = e.getWhen();

        for (TimeRange t : blockedTimes){
          // if overlap, check if start or duration of blocked region grows
          
          // if need to adjust start time
          if (eventTime.getStartTime() + eventTime.getDuration() > t.getStartTime()){
            
            blockedTimes.add(TimeRange.fromStartEnd(eventTime.getStartTime(), t.getStartTime() + t.getDuration(), false));
            // remove t in a safer way than blockedTimes.remove(t); cause iterating
          }

          // if need to adjust end time
          if (){

          }

          // if no overlap, don't merge, just add it
          if (){
            blockedTimes.add(eventTime);
          }
        }

      }
    }

    // Sort by start time (not the most efficient, but works for our purposes)
    //Collections.sort(blockedTimes);


    // Look through all possible times for a duration of sufficient length
    Collection<TimeRange> meetingTimes = new ArrayList<TimeRange>();

    // take difference?

    // Return meeting times (could be empty)
    return meetingTimes;
  }
}
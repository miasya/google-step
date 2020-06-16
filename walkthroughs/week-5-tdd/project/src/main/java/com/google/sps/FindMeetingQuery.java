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

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {


  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    ArrayList<TimeRange> freeTimes = new ArrayList<TimeRange>();

    // Initialize freeTimes with all 30 minute blocks since requests and events will only be
    // in chunks of 30 minutes (for more granularity, make it a day full of 1 min blocks)
    int blockLength = 30;
    int blocks[] = {0, 30};

    for (int i = 0; i < 24; i++){
      for (int b : blocks){
        //
        int startTime = TimeRange.getTimeInMinutes(i, b);
        freeTimes.add(TimeRange.fromStartDuration(startTime, blockLength));
      }
    }

    // Get important request information
    Collection<String> attendees = request.getAttendees();
    long meetingDuration = request.getDuration();

    // For all events in event
    for (Event e : events){

      // Check if at least one request attendee is an event attendee, otherwise skip event
      ArrayList<String> eventAttendees = new ArrayList<String>(e.getAttendees());
      eventAttendees.retainAll(attendees);
      if (eventAttendees.size() != 0){

        // Remove from list of free times
        TimeRange eventTime = e.getWhen();
        System.out.println(eventTime);
        
        
        // TODO: make sure it works for multiples of 30 mins
        
        freeTimes.remove(eventTime);

      }
    }
    

    Collection<TimeRange> meetingTimes = new ArrayList<TimeRange>();

    // TODO: Merge consecutive times blocks
    while (freeTimes.size() != 0) {
      System.out.println("next block");
      int i = 1;

      // Run until disconnect
      while (i < freeTimes.size() && freeTimes.get(i-1).end() == freeTimes.get(i).start()){
        i++;
      }

      // TODO: Filter out the ones that are too short
      meetingTimes.add(TimeRange.fromStartEnd(freeTimes.get(0).start(), freeTimes.get(i-1).end(), false));
      for (int j = 0; j < i; j++){
        System.out.println("removing " + freeTimes.get(0));
        freeTimes.remove(0);
      }
    }

    



    // Return meeting times (could be empty)
    return meetingTimes;
  }
}
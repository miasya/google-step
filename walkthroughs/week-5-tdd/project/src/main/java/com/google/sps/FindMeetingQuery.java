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
    
    // Initialize freeTimes with all 30 minute blocks since requests and events will only be
    // in chunks of 30 minutes (for more granularity, make it a day full of 1 min blocks)
    ArrayList<TimeRange> freeTimes = new ArrayList<TimeRange>();

    int blockLength = 30;
    int blocks[] = {0, 30};

    for (int i = 0; i < 24; i++){
      for (int b : blocks){
        int startTime = TimeRange.getTimeInMinutes(i, b);
        freeTimes.add(TimeRange.fromStartDuration(startTime, blockLength));
      }
    }

    // Get important request information
    Collection<String> meetingAttendees = request.getAttendees();
    long meetingDuration = request.getDuration();

    // Consider all events
    for (Event e : events){

      // Check if the event has at least one meetingAttendee (otherwise just skip the event)
      ArrayList<String> eventAttendees = new ArrayList<String>(e.getAttendees());
      eventAttendees.retainAll(meetingAttendees);

      if (eventAttendees.size() != 0){

        // Remove all busy time blocks (those in the event) from list of free times
        TimeRange eventTime = e.getWhen();
        int numBlocks = eventTime.duration() / blockLength;
        for (int i = 0; i < numBlocks; i++){
          freeTimes.remove(TimeRange.fromStartDuration(eventTime.start() + i * blockLength, blockLength));
        }
      }
    }
    
    // Now we merge consecutive freetimes of size blockLength to give big ranges
    // As well as eliminate blocks of free time too small to accomodate the request
    Collection<TimeRange> meetingTimes = new ArrayList<TimeRange>();

    // Merge consecutive times blocks
    while (freeTimes.size() != 0) {
      System.out.println("next block");
      int i = 1;

      // Run until there are not consecutive
      while (i < freeTimes.size() && freeTimes.get(i-1).end() == freeTimes.get(i).start()){
        i++;
      }

      // Now we have a potential uninterrupted time block to add
      TimeRange potentialTime = TimeRange.fromStartEnd(freeTimes.get(0).start(), freeTimes.get(i-1).end(), false);

      // Verify that the timeblock is long enough before adding it as a potential meeting time
      if (potentialTime.duration() >= meetingDuration){
        meetingTimes.add(potentialTime);
      }

      // Move onto the next free times
      for (int j = 0; j < i; j++){
        freeTimes.remove(0);
      }
    }

    // Return all possible meeting times (could be empty)
    return meetingTimes;
  }
}
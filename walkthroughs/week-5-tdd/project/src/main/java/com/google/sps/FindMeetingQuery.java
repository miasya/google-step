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
    

    // TODO: ADD MORE COMMENTS
    // TODO: Fix addAll method bug


    // Get important request information
    Collection<String> allAttendees = request.getAttendees();
    allAttendees.addAll(request.getOptionalAttendees());
    Collection<String> mandatoryAttendees = request.getAttendees();
    long meetingDuration = request.getDuration();
    
    // First consider that all optional attendees are actually requires, and see if
    // possible times can be found. If no free times, exclude optional and return meeting times
    ArrayList<TimeRange> freeTimes = new ArrayList<TimeRange>();
    Collection<TimeRange> meetingTimes = new ArrayList<TimeRange>();

    populateFreeTimes(freeTimes, 30);
    removeBusyTimes(freeTimes, events, allAttendees, 30);
    updateMeetingTimes(meetingTimes, freeTimes, meetingDuration);
    
    // Verify that there is at least one potential meeting time with all attendees
    if (meetingTimes.size() != 0) {
      return meetingTimes;
    }

    // Consider only mandatory attendees
    populateFreeTimes(freeTimes, 30);
    removeBusyTimes(freeTimes, events, mandatoryAttendees, 30);
    updateMeetingTimes(meetingTimes, freeTimes, meetingDuration);
    return meetingTimes;
  }


  private void populateFreeTimes (ArrayList<TimeRange> freeTimes, int blockLength) {
    // Initialize freeTimes with all 30 minute blocks since requests and events will only be
    // in chunks of 30 minutes (for more granularity, make it a day full of 1 min blocks)
    ArrayList<Integer> blocks = new ArrayList<Integer>();
    for (int i = 0; i < 60; i++){
      if (i % blockLength == 0){
        blocks.add(i);
      }
    }

    for (int i = 0; i < 24; i++){
      for (int b : blocks){
        int startTime = TimeRange.getTimeInMinutes(i, b);
        freeTimes.add(TimeRange.fromStartDuration(startTime, blockLength));
      }
    }
  }

  private void removeBusyTimes(ArrayList<TimeRange> freeTimes, Collection<Event> events, Collection<String> meetingAttendees, int blockLength){
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
  }

  private void updateMeetingTimes(Collection<TimeRange> meetingTimes, ArrayList<TimeRange> freeTimes, long meetingDuration){
    // Now we merge consecutive freetimes of size blockLength to give big ranges
    // As well as eliminate blocks of free time too small to accomodate the request
    
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
  }
}

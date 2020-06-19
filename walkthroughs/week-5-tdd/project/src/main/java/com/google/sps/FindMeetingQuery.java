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
    /* Takes a meeting request that has mandatory attendees, optional attendees, and a duration
     * then returns a sorted Collection of all possible TimeRanges of when the meeting could take place.
     * 
     * This function works by first initializing a list of free blocks of a certain length
     * Then it goes through events and removes free blocks when they overlap with an event.
     * It then merges the remaining consecutive free blocks so they're outputted as one
     * longer continuous TimeRange and we can verify that they're at least the length of the
     * requested duration. Note that the function first tries to include both optional and mandatory
     * attendees, but if no time ranges are found, only mandatory attendees are considered (if any).
     */

    // Get request information on attendees and duration
    Collection<String> allAttendees = new ArrayList<String>(request.getAttendees());
    allAttendees.addAll(request.getOptionalAttendees());
    Collection<String> mandatoryAttendees = new ArrayList<String>(request.getAttendees());
    long meetingDuration = request.getDuration();
    
    // First attempt to include both mandatory and optional attendees
    ArrayList<TimeRange> freeTimes = new ArrayList<TimeRange>();
    Collection<TimeRange> meetingTimes = new ArrayList<TimeRange>();

    // Initialize all free time blocks. Size 30 refers to the length of each block in minutes
    // (ie. blocks are 12:00am, 12:30am, 1:00am etc) since we assume all events begin on the half hour
    // Note that 15, 10, 5, 2, 1 minutes will all work but the granularity is not necessary
    populateFreeTimes(freeTimes, 30);

    // Remove from freeTimes all the TimeRanges where an attendee has an event
    removeBusyTimes(freeTimes, events, allAttendees, 30);

    // Make the remaining free blocks continuous and verify they are of minimum duration
    updateMeetingTimes(meetingTimes, freeTimes, meetingDuration);
    
    // Verify that there is at least one potential meeting time with all attendees
    // If not, only consider mandatory attendees (if any)
    if (meetingTimes.size() != 0) {
      return meetingTimes;
    }

    // Consider only mandatory attendees
    if (mandatoryAttendees.size() != 0) {
      freeTimes.clear();
      populateFreeTimes(freeTimes, 30);
      removeBusyTimes(freeTimes, events, mandatoryAttendees, 30);
      updateMeetingTimes(meetingTimes, freeTimes, meetingDuration);
    }
    return meetingTimes;
  }

  private void populateFreeTimes (ArrayList<TimeRange> freeTimes, int blockLength) {
    /* Populates FreeTimes with 1 day of blocks of size blockLength
     * (i.e. blocklength 15 would mean 12:00am, 12:15am, 12:30am, 12:45am, etc)
     * Note that blockLength must be a factor of 60 minutes or there may be unexpected behaviour.
     */

    // blocks holds all possible minutes value in the hour (ex: 0, 15, 30 if blockLength=15)
    ArrayList<Integer> blocks = new ArrayList<Integer>();
    for (int i = 0; i < 60; i++) {
      if (i % blockLength == 0){
        blocks.add(i);
      }
    }

    // Save all TimeRanges
    for (int i = 0; i < 24; i++) {
      for (int b : blocks){
        int startTime = TimeRange.getTimeInMinutes(i, b);
        freeTimes.add(TimeRange.fromStartDuration(startTime, blockLength));
      }
    }
  }

  private void removeBusyTimes(ArrayList<TimeRange> freeTimes, Collection<Event> events, Collection<String> meetingAttendees, int blockLength) {
    /* Updates freeTimes in place by removing all TimeRanges which overlap with events where
     * at least one meeting attendee is attending.
     */
    
    // Consider all events
    for (Event e : events){

      // Check if the event has at least one meetingAttendee (otherwise just skip the event)
      ArrayList<String> eventAttendees = new ArrayList<String>(e.getAttendees());
      eventAttendees.retainAll(meetingAttendees);
      if (eventAttendees.size() != 0) {

        // Remove all busy time blocks (those in the event) from list of free times
        TimeRange eventTime = e.getWhen();
        int numBlocks = eventTime.duration() / blockLength;
        for (int i = 0; i < numBlocks; i++) {
          freeTimes.remove(TimeRange.fromStartDuration(eventTime.start() + i * blockLength, blockLength));
        }
      }
    }
  }

  private void updateMeetingTimes(Collection<TimeRange> meetingTimes, ArrayList<TimeRange> freeTimes, long meetingDuration){
    /* Update meetingTimes in place by adding blocks of uninterrupted freeTimes of minimum length meetingDuration.
     * This is done by merging continuous blocks of freeTime into one big range.
     */

    // Merge consecutive times blocks
    while (freeTimes.size() != 0) {
      int i = 1;

      // Run until there are not consecutive (i.e. looping stops when there's a "break)
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

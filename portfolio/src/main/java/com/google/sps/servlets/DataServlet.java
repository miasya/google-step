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
 
package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
 
  public class Comment {
    long id;
    String page;
    String text;
    String nickname;
    String timeString;

    // Necessary to implement delete query functionality *soon*
    public void setId(long id){
      this.id = id;
    }

    // Webpage where comment originated
    public void setPage(String page){
      this.page = page;
    }

    // Inner contents of the comment
    public void setText(String text) {
      this.text = text;
    }

    // Don't want to return the entire email for privacy reasons
    public void setNickname(String email) {
      if (email != null && email.length() > 6){
        this.nickname = email.substring(0, 6);
      } else {
        this.nickname = "Unknown";
      }
    }

    // Use timestamp to create a human-friendly timeString
    public void setTimeString(long timestamp) {
      Date time = new Date(timestamp);
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
      dateFormat.setTimeZone(TimeZone.getTimeZone("Canada/Eastern"));
      this.timeString = dateFormat.format(time);
    }
  }

  public List<Comment> comments;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Specify desired entity from datastore
    Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    comments = new ArrayList<>();

    // Check all entities returned and store the relevant ones in comments
    for (Entity entity : results.asIterable()) {

      // Skip all comments that belong to other pages
      String commentPage = (String) entity.getProperty("page");
      String currentPage = request.getHeader("referer").replace("\n", "");
      if (!commentPage.equals(currentPage)){
        continue;
      }

      Comment newComment = new Comment();
      newComment.setPage((String) entity.getProperty("page"));
      newComment.setId(entity.getKey().getId());
      newComment.setText((String) entity.getProperty("text"));
      newComment.setNickname((String) entity.getProperty("email"));
      newComment.setTimeString((long) entity.getProperty("timestamp"));

      comments.add(newComment);
    }

    // Convert to JSON
    String json = convertToJsonUsingGson(comments);
 
    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
 
   /**
   * Converts an object instance into a JSON string using the Gson library. Note: We first added
   * the Gson library dependency to pom.xml.
   */
  private String convertToJsonUsingGson(List comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();

    // Only logged-in users can post messages
    if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL(request.getHeader("referer"));
      response.sendRedirect(loginUrl);
      return;
    }

    // Get the input from the form and a current timestamp
    String text = request.getParameter("text-input");
    long timestamp = System.currentTimeMillis();
    String email = userService.getCurrentUser().getEmail();

    // Save as entity in Datastore
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity taskEntity = new Entity("Task");
    taskEntity.setProperty("text", text);
    taskEntity.setProperty("email", email);
    taskEntity.setProperty("timestamp", timestamp);
    taskEntity.setProperty("page", request.getHeader("referer"));

    datastore.put(taskEntity);
    
    // Redirect back to the recipe HTML page it came from
    response.sendRedirect(request.getHeader("referer"));
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
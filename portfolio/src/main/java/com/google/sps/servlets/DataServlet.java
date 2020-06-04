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
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
 
  private List<String> comments;
 
  @Override
  public void init() {
    comments = new ArrayList<>();
  }
 
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
 
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
    // Get the input from the form.
    String text = getParameter(request, "text-input", "");
    comments.add(text);

    // TODO: Add timestamp or other options to the comment

    // Respond with the result.
    response.setContentType("text/html;");
    response.getWriter().println(text);

     // Redirect back to the HTML page.
     // TODO: Make this work with every recipe page when extra time(store the current page address)
    response.sendRedirect("/recipe-1.html");
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

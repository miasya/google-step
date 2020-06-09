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
 
fetch('header.html')
  .then(response => {
    return response.text();
  })
  .then(data => {
    document.querySelector('header').innerHTML = data;
  });

fetch('footer.html')
  .then(response => {
    return response.text();
  })
  .then(data => {
    document.querySelector('footer').innerHTML = data;
  });

/**
 * Allows user to log in using Google's User API (see LoginServlet.java)
 */
function userLogin() {
  fetch('/login').then(response => response.json()).then((msg) => {

  });
}

function getLoginStatus() {
  fetch('/login-status').then(response => response.json()).then((msg) => {
    const loginContainer = document.getElementById('login-container');
    const loginStatus = msg.login;
    loginContainer.innerText = loginStatus;
  });
}

/**
 * Fetches comments from the servers and adds them to the DOM.
 */
function getComments() {
  fetch('/data').then(response => response.json()).then((comments) => {
    // Reference its fields to create HTML content
    const dataListElement = document.getElementById('comments-container');
    dataListElement.innerHTML = '';
    var i;
    for (i = 0; i < comments.length; i++) {
      console.log(i);
      dataListElement.appendChild(
        createCommentElement(comments[i]));
    }
  });
}

/** Creates an <li> element containing text. */
function createCommentElement(comment) {

  const titleElement = document.createElement('h4');
  titleElement.innerHTML = comment.nickname + " at " + comment.timeString + " Eastern";
  
  const textElement = document.createElement('p');
  textElement.innerHTML = comment.text;
 
  var divElement = document.createElement('div');
  divElement.appendChild(titleElement);
  divElement.appendChild(textElement);

  return divElement;
}



/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['I will work soon I promise!', 'feature to be added!', 'not possible yet but just wait!'];
 
  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];
 
  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}
 
/* Toggle between showing and hiding the navigation menu links when the user clicks on the hamburger menu / bar icon */
function navBar() {
  var x = document.getElementById("myTopnav");
  if (x.className === "topnav") {
    x.className += " responsive";
  } else {
    x.className = "topnav";
  }
}
 
/* Slideshow functions */
var slideIndex = 1;
showSlides(slideIndex);
 
// Next/previous controls
function plusSlides(n) {
  showSlides(slideIndex += n);
}
 
// Thumbnail image controls
function currentSlide(n) {
  showSlides(slideIndex = n);
}
 
function showSlides(n) {
  var i;
  var slides = document.getElementsByClassName("mySlides");
  var dots = document.getElementsByClassName("dot");
  if (n > slides.length) {slideIndex = 1}
  if (n < 1) {slideIndex = slides.length}
  for (i = 0; i < slides.length; i++) {
      slides[i].style.display = "none";
  }
  for (i = 0; i < dots.length; i++) {
      dots[i].className = dots[i].className.replace(" active", "");
  }
  slides[slideIndex-1].style.display = "block";
  dots[slideIndex-1].className += " active";
}
 
 
// Random word card display
var wordIndex = 1;
 
showRandomWord();
 
function showRandomWord() {
  const wordList = ['Esoteric', 'Didactic', 'Sanguine'];
 
  wordIndex = Math.floor(Math.random() * wordList.length);
  
  // update word
  const wordBox = document.getElementById('word-box');
  wordBox.innerText = wordList[wordIndex];
  
  // remove old definition
  const defBox = document.getElementById('def-box');
  defBox.innerText = '';
}
 
function showDef() {
  const defList = ['mysterious, obscure\nA couple of months ago, XYZ submitted a thesis with their analysis and computations — a fairly esoteric mathematical dissent about how best to gather rational generalizations on the origin of the universe theory.',
    'intended to teach, educational\nThough more didactic, XYZ’s story of the triumph over evil quite powerful and enchanting.',
    'optimistic or positive\nAmong those who remain sanguine about the nation’s economic revival, there is always the lively topic of tax reduction policies.'];
 
  const defBox = document.getElementById('def-box');
  defBox.innerText = defList[wordIndex];
}

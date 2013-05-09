(function() {
  var events = {log: []};
  var session = sessionID();

  if ((typeof require != 'undefined') && 
      (typeof define == 'undefined')) {
    try {
      var user = require('zooniverse/lib/modles/user');
    } catch (e) {
      var user = require('lib/user');
    }
  } else {
    var user = {current: {id: "Not Logged In"}};
  }

  function sessionID() {
    var guid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
      return v.toString(16);
    });
    return guid;
  }

  function send(auth, data) {
    var xhr = new XMLHttpRequest();

    xhr.onload = function() {
      if (xhr.status == 201) {
        timeout = parseInt(xhr.response);
        setTimeout(postLog, timeout, auth);
      } else {
        throw new Error("Failed to Post to Server");
      }
    };
    xhr.open("POST", "http://localhost:3000/events/", true);
    xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
    xhr.setRequestHeader("Accept", "text/plain")
    if (typeof auth == 'object') {
      authStr = btoa(auth.site + ":" + auth.key);
      xhr.setRequestHeader("Authorization", "Basic " + authStr);
    }
    xhr.send(JSON.stringify(data));
  }

  function logEvent(e) {
    var currentUser;
    if ((typeof user.current == 'undefined') ||
        (user.current == null))
      currentUser = "Not Logged-In"
    else
      currentUser = user.current.id
    e.user = currentUser;
    e.session = session;
    e.created_at = new Date();
    e.window_height = window.innerHeight;
    e.window_width = window.innerWidth;
    e.path_name = location.pathname + location.hash
    events.log.push(e);
  }

  function logDomEvent(e) {
    event = {
      type: e.type,
      tag: e.target.tagName,
      id_name: e.target.id,
      class_name: e.target.className,
      position_x: e.clientX,
      position_y: e.clientY,
      value: (e.target.value || "")
    };
    logEvent(event);
  }

  function logCustomEvent(e) {
    if ((typeof e.type == 'undefined') || (typeof e.value == 'undefined'))
      throw new Error("Events Must Have a Type and Value");
    else
      logEvent(e);
  }

  function postLog(auth) {
    if (events.log.length != 0) {
      send(auth, events);
      events.log = [];
    } else {
      setTimeout(postLog, 5000, auth);
    }
  }

  function init(site, key) {
    var auth = {site: site, key: key};
    document.addEventListener("click", logDomEvent, true);
    document.addEventListener("change", logDomEvent, true);
    setTimeout(postLog, 5000, auth);
  }

  sleuth = {
    logCustomEvent: logCustomEvent,
    init: init
  };

  this.sleuth = sleuth;

}).call(this);
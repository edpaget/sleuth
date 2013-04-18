(function() {
  var events = {log: []}
  if ((typeof require != 'undefined') && (typeof define == 'undefined'))
    try {
      var user = require('zooniverse/lib/modles/user');
    } catch (e) {
      var user = require('lib/user');
    }
  else
    var user = {current: {id: ""}};

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
    var current_user;
    if ((typeof user.current == 'undefined') ||
        (user.current == null))
      current_user = "Not Logged-In"
    else
      current_user = user.current.id

    events.log.push({
      tag: e.target.tagName,
      idName: e.target.id,
      className: e.target.className,
      position: { x: e.clientX, y: e.clientY},
      user: current_user,
      value: e.target.value,
      dataset: e.target.dataset
    });
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
    document.addEventListener("click", logEvent, true);
    setTimeout(postLog, 5000, auth);
  }

  this.init = init;

}).call(this);
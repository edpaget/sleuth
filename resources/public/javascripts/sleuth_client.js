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
      console.log('here');
      if (xhr.status == 201) {
        timeout = parseInt(xhr.resposne);
        setTimeout(postLog, 500, auth);
      } else {
        throw new Error("Failed to Post to Server");
      }
    };
    xhr.open("POST", "http://localhost:3000/events/", true);
    xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
    if (typeof auth == 'object') {
      authStr = btoa(auth.site + ":" + auth.site_key);
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
    send(auth, events);
    events.log = [];
  }

  function init(site, key) {
    var auth = {site: site, key: key};
    document.addEventListener("click", logEvent, true);
    setTimeout(postLog, 5000, auth);
  }

  this.init = init;

}).call(this);
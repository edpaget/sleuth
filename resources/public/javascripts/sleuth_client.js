(function() {
  var events = {log: []}
  if ((typeof require != 'undefined') && (typeof define == 'undefined'))
    var user = require('zooniverse/lib/modles/user');
  else
    var user = {current: {id: ""}};

  function send(auth, data) {
    var xhr = new XMLHttpRequest();

    xhr.onload = function() {
      if (xhr.status == 201) {
        timeout = parseInt(xhr.resposne);
        setTimeout(5000, postLog);
      } else {
        throw new Error("Failed to Post to Server");
      }
    };
    xhr.open("POST", "/events", true);
    xhr.setRequestHeader("Content-Type", "applicaiton/json");
    if (typeof auth == 'object') {
      authStr = bota(auth.site + ":" + auth.site_key);
      xhr.setRequestHeader("Authorization", "Basic " + authStr);
    }
    xhr.send(JSON.stringify(data));
  }

  function logEvent(e) {
    events.log.push({
      tag: e.target.tagName,
      idName: e.target.id,
      className: e.target.className,
      position: { x: e.clientX, y: e.clientY},
      user: user.current.id,
      value: e.target.value,
      dataset: e.target.dataset
    });
  }

  function postLog(auth, data) {
    send(auth, data);
    eventsLog = [];
  }

  function init(site, key) {
    var auth = {site: site, key: key};
    document.addEventListener("click", logEvent, true);
    setTimeout(5000, postLog, auth, data);
  }

}).call(this);
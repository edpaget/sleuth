(function() {
  var $ = window.jQuery
  var eventsLog = []
  if ((typeof require != 'undefined') && (typeof define == 'undefined')) {
    var User = require('zooniverse/lib/modles/user');
  }

  function init(site, key, jQuery) {
    if (typeof jQuery != 'undefined')
      $ = jQuery;
    this.site = site
    this.key = key

    definitions = $.ajax('http://localhost:3000/define/' + site, {
      crossDomain: true,
      beforeSend: function(xhr) {
        var auth = btoa(site + ":" + key);
        xhr.setRequestHeader('Authorization', "Basic " + auth);
      }
    });
    defintions.done(setHandlers);
  }

  function setHandlers(defintions) {
    this.pages = definitions.pages;
    this.sessionId = definintions.sessionId
    $(window).on 'hashchange', loadPageEvents();
    loadPageEvents();
  }

  function loadPageEvents() {
    var hash = location.hash

    if (typeof this.events != 'undefined') {
      for (var event in this.events) {
        remove(event);
      }
    }

    for(var page in this.pages) {
      pageRegex = new RegExp(page.hash);
      if (hash.match(pageRegex) != null)
        this.events = page.events;
    }

    for (var event in this.events) {
      register(event);
    }
  }

  function register(event) {
    $(document).on(event.type, event.selector, logEvent(event.properties));
  }

  function remove(event) {
    $(document).off(event.type, event.selector);
  }

  function logEvent(properties) {
    return(function(ev) {
      eventsLog.push({
        type: ev.type,
        page: location.hash,
        pageX: ev.pageX,
        pageY: ev.pageY,
        time: new Date(),
        project: location.hostname,
        value: ev.target.value,
        dataset: ev.target.dataset,
        "class": ev.target.class,
        id: ev.target.id
      });
    });
  }
}).call(this);
(function ($, dust, exports) {
  "use strict";

  var filters = {
    'fmt_bytes': function (v) {
      var UNITS = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB'];
      var prev = 0, i = 0;
      while (Math.floor(v) > 0 && i < UNITS.length) {
        prev = v;
        v /= 1024;
        i += 1;
      }

      if (i > 0) {
        v = prev;
        i -= 1;
      }
      return Math.round(v * 100) / 100 + ' ' + UNITS[i];
    },

    'fmt_percentage': function (v) {
      return Math.round(v * 100) / 100 + '%';
    },

    'fmt_time': function (v) {
      var s = Math.floor(v / 1000), h = Math.floor(s / 3600);
      s -= h * 3600;
      var m = Math.floor(s / 60);
      s -= m * 60;

      var res = s + " sec";
      if (m !== 0) {
        res = m + " mins, " + res;
      }

      if (h !== 0) {
        res = h + " hrs, " + res;
      }

      return res;
    },

    'date_tostring' : function (v) {
      return moment(Number(v)).format('ddd MMM DD HH:mm:ss ZZ YYYY');
    },

    'format_compile_info' : function (v) {
      var info = v.split(" by ")
      var date = moment(info[0]).format('ddd MMM DD HH:mm:ss ZZ YYYY');
      return date.concat(" by ").concat(info[1]);
     },

    'helper_to_permission': function (v) {
      var symbols = [ '---', '--x', '-w-', '-wx', 'r--', 'r-x', 'rw-', 'rwx' ];
      var vInt = parseInt(v, 8);
      var sticky = (vInt & (1 << 9)) != 0;

      var res = "";
      for (var i = 0; i < 3; ++i) {
        res = symbols[(v % 10)] + res;
        v = Math.floor(v / 10);
      }

      if (sticky) {
        var otherExec = (vInt & 1) == 1;
        res = res.substr(0, res.length - 1) + (otherExec ? 't' : 'T');
      }

      return res;
    },

    'helper_to_directory' : function (v) {
      return v === 'DIRECTORY' ? 'd' : '-';
    },

    'helper_to_acl_bit': function (v) {
      return v ? '+' : "";
    },

    'fmt_number': function (v) {
      return v.toLocaleString();
    },

    'fmt_human_number': function (v) {
      var UNITS = ['', 'K', 'M'];
      var prev = 0, i = 0;
      while (Math.floor(v) > 0 && i < UNITS.length) {
        prev = v;
        v /= 1000;
        i += 1;
      }

      if (i > 0) {
        v = prev;
        i -= 1;
      }
      return Math.round(v * 100) / 100 + UNITS[i];
    }
  };
  $.extend(dust.filters, filters);

  /**
   * Load a sequence of JSON.
   *
   * beans is an array of tuples in the format of {url, name}.
   */
  function load_json(beans, success_cb, error_cb) {
    var data = {}, error = false, to_be_completed = beans.length;

    $.each(beans, function(idx, b) {
      if (error) {
        return false;
      }
      $.get(b.url, function (resp) {
        data[b.name] = resp;
        to_be_completed -= 1;
        if (to_be_completed === 0) {
          success_cb(data);
        }
      }).fail(function (jqxhr, text, err) {
        error = true;
        error_cb(b.url, jqxhr, text, err);
      });
    });
  }

  exports.load_json = load_json;

}($, dust, window));

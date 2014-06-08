// Some general UI pack related JS
// Extend JS String with repeat method
// String.prototype.repeat = function(num) {
//   return new Array(num + 1).join(this);
// };

(function($) {

  // Add segments to a slider
//  $.fn.addSliderSegments = function (amount, orientation) {    
//    return this.each(function () {
//      if (orientation == "vertical") {
//        var output = ''
//          , i;
//        for (i = 1; i <= amount - 2; i++) {
//          output += '<div class="ui-slider-segment" style="top:' + 100 / (amount - 1) * i + '%;"></div>';
//        };
//        $(this).prepend(output);
//      } else {
//        var segmentGap = 100 / (amount - 1) + "%"
//          , segment = '<div class="ui-slider-segment" style="margin-left: ' + segmentGap + ';"></div>';
//        $(this).prepend(segment.repeat(amount - 2));
//      }
//    });
//  };

  $(function() {
  
    // Custom Selects
    $("select[name='stageId']").selectpicker({style: 'btn-primary', menuStyle: 'dropdown-inverse'});
    $("select[name='time']").selectpicker({style: 'btn-primary', menuStyle: 'dropdown-inverse'});
    $("select[name='timeFrame']").selectpicker({style: 'btn-primary', menuStyle: 'dropdown-inverse'});

    // Ajax Updates
    $(function() {
      $(".performanceEditable").each(function(){
        $('#' + this.id).editable({trigger : $("#span_" + this.id), action : "click"}, function(e){
          jsRoutes.controllers.Application.ajaxUpdatePerformance(e.target.selector.replace("#performanceId", ""), e.value).ajax({
            beforeSend: function() {},
            complete: function() {},
            success: function() {},
            error: function() {}
          })
        });
      });
      $(".stageEditable").each(function(){
        $('#' + this.id).editable({trigger : $("#span_" + this.id), action : "click"}, function(e){
          jsRoutes.controllers.Application.ajaxUpdateStage(e.target.selector.replace("#stageId", ""), e.value).ajax({
            beforeSend: function() {},
            complete: function() {},
            success: function() {},
            error: function() {}
          })
        });
      });
      $("#addHeart").on("click", function(){
        jsRoutes.controllers.Application.ajaxInsertHeart($("#festivalId").val()).ajax({
          beforeSend: function() {},
          complete: function() {},
          success: function() {
            $("#addHeart").css('display','none')
            $("#delHeart").css('display','block')
          },
          error: function() {}
        })
      })
      $("#delHeart").on("click", function(){
        jsRoutes.controllers.Application.ajaxDeleteHeart($("#festivalId").val()).ajax({
          beforeSend: function() {},
          complete: function() {},
          success: function(returnData) {
            $("#addHeart").css('display','block')
            $("#delHeart").css('display','none')
          },
          error: function() {}
        })
      })
    })
  });
})(jQuery);


//     // Todo list
//     $(".todo").on('click', 'li', function() {
//       $(this).toggleClass("todo-done");
//     });
// 
//     // Tooltips
//     $("[data-toggle=tooltip]").tooltip("show");
// 
//     // Tags Input
//     $(".tagsinput").tagsInput();
// 
//     // jQuery UI Sliders
//     var $slider = $("#slider");
//     if ($slider.length) {
//       $slider.slider({
//         min: 1,
//         max: 5,
//         value: 2,
//         orientation: "horizontal",
//         range: "min"
//       }).addSliderSegments($slider.slider("option").max);
//     }
// 
//     var $verticalSlider = $("#vertical-slider");
//     if ($verticalSlider.length) {
//       $verticalSlider.slider({
//         min: 1,
//         max: 5,
//         value: 3,
//         orientation: "vertical",
//         range: "min"
//       }).addSliderSegments($verticalSlider.slider("option").max, "vertical");
//     }
// 
//     // Placeholders for input/textarea
//     $(":text, textarea").placeholder();
// 
//     // Focus state for append/prepend inputs
//     $('.input-group').on('focus', '.form-control', function () {
//       $(this).closest('.input-group, .form-group').addClass('focus');
//     }).on('blur', '.form-control', function () {
//       $(this).closest('.input-group, .form-group').removeClass('focus');
//     });
// 
//     // Make pagination demo work
//     $(".pagination").on('click', "a", function() {
//       $(this).parent().siblings("li").removeClass("active").end().addClass("active");
//     });
// 
//     $(".btn-group").on('click', "a", function() {
//       $(this).siblings().removeClass("active").end().addClass("active");
//     });
// 
//     // Disable link clicks to prevent page scrolling
//     $(document).on('click', 'a[href="#fakelink"]', function (e) {
//       e.preventDefault();
//     });
// 
//     // Switch
//     $("[data-toggle='switch']").wrap('<div class="switch" />').parent().bootstrapSwitch();
// 
//         // Typeahead
//     if($('#typeahead-demo-01').length) {
//       $('#typeahead-demo-01').typeahead({
//         name: 'states',
//         limit: 4,
//         local: ["Alabama","Alaska","Arizona","Arkansas","California","Colorado","Connecticut",
//         "Delaware","Florida","Georgia","Hawaii","Idaho","Illinois","Indiana","Iowa","Kansas","Kentucky",
//         "Louisiana","Maine","Maryland","Massachusetts","Michigan","Minnesota","Mississippi","Missouri",
//         "Montana","Nebraska","Nevada","New Hampshire","New Jersey","New Mexico","New York","North Dakota",
//         "North Carolina","Ohio","Oklahoma","Oregon","Pennsylvania","Rhode Island","South Carolina",
//         "South Dakota","Tennessee","Texas","Utah","Vermont","Virginia","Washington","West Virginia","Wisconsin","Wyoming"]
//       });
//     }  
// 
//     // make code pretty
//     window.prettyPrint && prettyPrint();    

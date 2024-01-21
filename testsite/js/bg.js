jQuery(function() {
    var theWindow = jQuery(window),
        aspectRatio   = jQuery("#bg").width() / jQuery("#bg").height();

    function resizeBg() {
        if ( (theWindow.width() / theWindow.height()) < aspectRatio ) {
            jQuery("#bg").removeClass().addClass('bgheight');
        } else {
            jQuery("#bg").removeClass().addClass('bgwidth');
        }
    }

    theWindow.resize(function() {
        resizeBg();
    }).trigger("resize");
});
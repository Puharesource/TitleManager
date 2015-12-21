var frames = [];

/*
 * Classes
 * ================
 */

var ErrorPopup = React.createClass({
  render: function() {

    return (

      <div className="ui error message">
        <i className="close icon"></i>
        <div className="header">
          An error occured!
        </div>
        <ul className="list">
          <li>Please resolve the issue!</li>
          <li>Right now!</li>
        </ul>
      </div>

    )

  }
});

var AnimationFrame = React.createClass({
  moveUp: function() {

  },

  moveDown: function() {

  },

  render: function() {

    var i = Number.parseInt(this.props.i);
    var canMoveUp = i !== 0;
    var canMoveDown = i + 1 === frames.length;

    return(

      <div id={'frame-' + i} className="ui grid">
        <div className="four wide column">
          <div className="ui basic buttons">
            <button onClick={canMoveUp ? this.moveUp : null} className={canMoveUp ? 'ui black button' : 'ui black button disabled'}>
              <i className="fa fa-arrow-up"></i>
            </button>
            <button onClick={canMoveDown ? this.moveDown : null} className={canMoveDown ? 'ui black button' : 'ui black button disabled'}>
              <i className="fa fa-arrow-down"></i>
            </button>
            <button className="ui blue button">
              <i className="fa fa-pencil"></i>
            </button>
            <button className="ui red button">
              <i className="fa fa-minus"></i>
            </button>
          </div>
        </div>

        <div id={'frame-' + i + '-text'} className="twelve wide column mc-font mc-colors">{this.props.text}</div>
      </div>
    );
  }
});

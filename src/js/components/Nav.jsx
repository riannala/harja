import React, {PropTypes} from 'react';
import {Events, Category} from '../enums.js';
import NavButton from './NavButton.jsx'
import pubsub from 'pubsub-js';


export default React.createClass({
  onclick: function() {
    pubsub.publish(Events.NAV, {action: Events.HOME});
  },

  render() {
    const largeButtonLinks = [
      {title: 'HARJA-PROJEKTI', data: {action: Events.HOME}, buttonStyle: 'large button'},
      {title: 'TEIDEN HOITO', data: {action: Events.CATEGORY, category: Category.CARE}, buttonStyle: 'large button'},
      {title: 'TEIDEN YLLÄPITO', data: {action: Events.CATEGORY, category: Category.MAINTENANCE}, buttonStyle: 'large button'},
      {title: 'KOULUTUSVIDEOT', data: {action: Events.CATEGORY, category: Category.FAQ}, buttonStyle: 'large button'}
    ];

    const normalButtonLinks = [
      {title: 'HARJA-PROJEKTI', data: {action: Events.HOME}},
      {title: 'TEIDEN HOITO', data: {action: Events.CATEGORY, category: Category.CARE}},
      {title: 'TEIDEN YLLÄPITO', data: {action: Events.CATEGORY, category: Category.MAINTENANCE}},
      {title: 'KOULUTUSVIDEOT', data: {action: Events.CATEGORY, category: Category.FAQ}}
    ];

    return (
      <nav>
        <div className="harja-header title-bar show-for-large">
          <div className="top-bar-left">
            <img className="livi-header-logo" src="images/livi_logo_blue.png" alt="liikennevirasto logo" />
            <img className="harja-header-logo" src="images/harja_logo_text.png" alt="harja logo" srcSet="images/harja_logo_text.svg" />
          </div>
          <div className="top-bar-right">
            <div className="title-bar-title">Liikenneviraston Harja-projekti</div>
          </div>
        </div>

        <div className="harja-title-bar title-bar" data-responsive-toggle="top-menu" data-hide-for="large">
          <div className="top-bar-left">
            <a href="#" onClick={this.onclick}>
              <img className="harja-menu-logo" src="images/harja_logo.png" alt="harja logo" />
            </a>
          </div>
          <div className="top-bar-right">
            <button className="harja-icon-menu" type="button" data-toggle="top-menu"></button>
          </div>
        </div>

        <div className="harja-menu top-bar" id="top-menu">
          <div className="top-bar-left">
            <ul className="vertical large-horizontal menu hide-for-large" data-responsive-menu="large-dropdown">
              {
                largeButtonLinks.map((link, index) =>
                  <li key={index}>
                    <NavButton item={link} key={link.title} />
                  </li>)
              }
              <li>
                <a className="large button" href="apidoc/api.html" target="_blank">Katso API</a>
              </li>
              <li >
                <a className="large button" href="https://github.com/finnishtransportagency/harja" target="_blank">Projekti GitHubissa</a>
              </li>
            </ul>

            <ul className="vertical large-horizontal menu show-for-large " data-responsive-menu="large-dropdown">
              {
                normalButtonLinks.map((link, index) =>
                  <li key={index}>
                    <NavButton item={link} key={link.title} />
                  </li>)
              }
            </ul>
          </div>
        </div>
      </nav>
    );
  }
});
import React, { Component } from 'react';
import NavItem from 'react-bootstrap/lib/NavItem';
import Navbar from 'react-bootstrap/lib/Navbar';
import Nav from 'react-bootstrap/lib/Nav';
import ProgressPoll from './progress_poll';
import screenfull from 'screenfull';
import { FocusableSection, Focusable } from 'react-js-spatial-navigation'
import FocusableLinkContainer from './focusable_link_container'
import LinkContainer from 'react-router-bootstrap/lib/LinkContainer';


export default class Selectables extends Component {

    handleZoom() {

        if (screenfull.enabled) {
            screenfull.toggle(document.getElementsByClassName("ReactModalPortal")[0]);
        }

    }

    render() {

        return(



        );

    }

}
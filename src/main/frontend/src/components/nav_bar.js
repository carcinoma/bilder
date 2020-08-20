import React, { Component } from 'react';
import NavItem from 'react-bootstrap/lib/NavItem';
import Navbar from 'react-bootstrap/lib/Navbar';
import Nav from 'react-bootstrap/lib/Nav';
import ProgressPoll from './progress_poll';
import screenfull from 'screenfull';
import { FocusableSection, Focusable } from 'react-js-spatial-navigation'
import FocusableLinkContainer from './focusable_link_container'


export default class NavBar extends Component {

    handleZoom() {

        if (screenfull.enabled) {
            screenfull.toggle(document.getElementsByClassName("ReactModalPortal")[0]);
        }

    }

    render() {

        return(

            <Navbar inverse collapseOnSelect fixedTop fluid>
                <Navbar.Header>
                    <Navbar.Brand>Bilder</Navbar.Brand>
                    <Navbar.Toggle />
                </Navbar.Header>
                <Navbar.Collapse>
                    <FocusableSection enterTo='last-focused'>

                        <Nav>
                            <FocusableLinkContainer exact to="/" name="Ordner" />
                            <FocusableLinkContainer exact to="/date" name="Datum" />
                            <FocusableLinkContainer exact to="/camera" name="Kamera" />
                            <FocusableLinkContainer exact to="/tags" name="Tags" />
                        </Nav>

                        <Nav pullRight>
                            <ProgressPoll />
                            <NavItem className="focusable-second zoom_out"
                                        onSelect={this.handleZoom.bind(this)}
                                        href="#">
                                <Focusable onClickEnter={this.handleZoom.bind(this)} >
                                    <i className="material-icons">zoom_out_map</i>
                                </Focusable>
                            </NavItem>
                        </Nav>
                    </FocusableSection>
                </Navbar.Collapse>
            </Navbar>

        );

    }

}
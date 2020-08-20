import React, { Component } from 'react';
import NavItem from 'react-bootstrap/lib/NavItem';
import LinkContainer from 'react-router-bootstrap/lib/LinkContainer';
import { Focusable } from 'react-js-spatial-navigation'


export default class FocusableLinkContainer extends Component {

    handleClickEnter(e) {
        e.button = 0;
        this.refs.linkContainer.handleClick(e);
    }

    render() {

        return(

            <LinkContainer ref="linkContainer" exact={this.props.exact} to={this.props.to}>
                <NavItem>
                    <Focusable onClickEnter={this.handleClickEnter.bind(this)}>{this.props.name}</Focusable>
                </NavItem>
            </LinkContainer>

        );

    }

}
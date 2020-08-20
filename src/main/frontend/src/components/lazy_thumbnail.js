import React, { Component } from 'react';
import Thumbnail from 'react-bootstrap/lib/Thumbnail';
import LazyLoad from 'react-lazy-load';
import {apiBase} from '../constants'
import { Focusable } from 'react-js-spatial-navigation'

export default class LazyThumbnail extends Component {

    constructor(props) {
        super(props);
        this.toucheMove=false;
        this.state = {
          selected: false,
          tags: props.image.tags
        };
    }    

    componentDidMount() {
        this.props.register(this.props.image.id, this);
    }

    shouldComponentUpdate(newProps, newState, newContext) {
        let selectedChanged = (newState.selected !== this.state.selected);
        let tagsChanged = false;
        if(newState.tags && this.state.tags) {
            tagsChanged = (newState.tags.length !== this.state.tags.length);
        }
        return (selectedChanged || tagsChanged);
    }

    handleClick(e) {
        if(!this.toucheMove) {
            this.props.onClick({target: this.refs.focusable.el.firstChild});
        }
        this.toucheMove == false;
    }

    handleFocus(e) {
        this.refs.focusable.el.focus();
        this.props.onFocus({target: this.refs.focusable.el, detail: e.detail});
    }

    handleTouchMove(e) {
        this.toucheMove=true;
    }

    render() {

        const pathName = this.props.pathName;
        const image = this.props.image;
        const tags = this.state.tags;
        const imageName = this.props.imageName;
        
        return (
            <div id={image.id}>
                <LazyLoad
                    debounce={false}
                    throttle={100}
                    offsetHorizontal={1000}
                    onContentVisible={this.props.onObjectLoaded}
                    className="selectable">

                    <Focusable onFocus={this.handleFocus.bind(this)}
                               onClickEnter={this.handleClick.bind(this)}
                                onKeyDown={this.props.onKeyDown}
                               ref="focusable">

                        <Thumbnail data-path={pathName}
                                data-image={imageName}
                                data-id={image.id}
                                onKeyDown={this.props.onKeyDown}
                                onDoubleClick={this.handleClick.bind(this)}
                                onTouchEnd={this.handleClick.bind(this)}
                                onTouchMove={this.handleTouchMove.bind(this)}
                                className={`image-thumb focusable ${ this.state.selected ? "selected" : "" }`}
                                src={`${apiBase}${image.thumb}`}
                                alt={imageName}
                                >
                            <p>{imageName}</p>
                            {tags && tags.length > 0 &&
                                <span className="badge" title={tags}>{tags.length}</span>
                            }
                        </Thumbnail>

                    </Focusable>

                </LazyLoad>     
                </div>
        );

    }

}
import React, { Component } from 'react';
import LazyLoad from 'react-lazy-load';
import {apiBase} from '../constants'
import LazyThumbnail from './lazy_thumbnail';
import axios from 'axios';
import Row from 'react-bootstrap/lib/Row';
import Col from 'react-bootstrap/lib/Col';

export default class LazyRow extends Component {

    constructor(props) {
        super(props);
        this.firstRender=true;
        this.state = {
          images: props.images
        };
    }

    componentDidMount() {
        this.props.registerRow(this.props.rownName, this);
    }

    handleDelete(e) {
        axios.delete(apiBase + "api/tags/" + e.target.dataset.tag).then(res => {
            this.props.onChange(e);
        });
    }


    registerInternalNode(id, node) {
        this.props.register(this.props.rownName, this, id, node);
    }

    render() {

        const rownName = this.props.rownName;
        const images = this.state.images;
        const isTagsView = this.props.isTagsView;
        
        return (
            <LazyLoad debounce={false}
                throttle={100}
                height={285}
                offsetVertical={1000}>

                <Row>
                    <h4>
                        &nbsp;&nbsp;{rownName}&nbsp;
                        <small>({Object.keys(images).length} Bilder)</small>
                        {isTagsView && <a data-tag={rownName} onClick={this.handleDelete.bind(this)} className="tag-remove-button material-icons">delete_forever</a>}
                    </h4>
                    <Col xs={12} md={12} className="image-box style-4 style-4-1">
                        {Object.keys(images).map(image =>

                            <LazyThumbnail
                                key={`${rownName}_${image}`}
                                pathName={rownName}
                                image={images[image]}
                                imageName={image}
                                onKeyDown={this.props.onKeyDown}
                                onClick={this.props.onClick}
                                register={this.props.registerImage}
                                onFocus={this.props.onFocus}
                                />

                        )}
                    </Col>
                </Row>

            </LazyLoad>
        );

    }

}
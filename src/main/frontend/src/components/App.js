import React, { Component } from 'react';
import './App.css';
import axios from 'axios';
import Grid from 'react-bootstrap/lib/Grid';
import Row from 'react-bootstrap/lib/Row';
import Col from 'react-bootstrap/lib/Col';
import Jumbotron from 'react-bootstrap/lib/Jumbotron';
import {Wave} from 'better-react-spinkit'
import {connect} from 'react-redux';
import { withRouter } from 'react-router'
import SpatialNavigation from 'react-js-spatial-navigation'

import LazyRow from './lazy_row';
import LightImageBox from './light_image_box';
import TagModal from './tag_modal';
import NavBar from './nav_bar';
import {apiBase} from '../constants'
import {fetchImages, loading} from "../actions/index";

class App extends Component {

    constructor(props) {
        super(props);
        this.imageNodes=[];
        this.rowNodes=[];
        this.selectedImages=[];
        this.state = {
          images: null,
          shift: false,
          ctrl: false,
          isTagsView: false
        };
    }

    componentDidMount() {

        this.props.fetchImages(this.props.match.params.sort);

        const that=this;
        /*
        window.SpatialNavigation.init();
        window.SpatialNavigation.set({
            straightOnly: true
        });

        window.SpatialNavigation.add({
            selector: '.focusable'
        });

        window.addEventListener('sn:willfocus', function (e,a) {
            that.handleWillFocus(e);
        }, false);
  
        window.addEventListener('sn:focused', function (e,a) {
            that.handleFocus(e)
        }, false);

        window.addEventListener('sn:enter-up', function (e,a) {
          that.handleEnterUp(e)
        }, false);
        */
        document.body.onkeydown = function(e) {
            if (e.which === 16) {
                that.state.shift = true;
            }
            if (e.which === 17) {
                that.state.ctrl = true;
            }
        };

        document.body.onkeyup = function(e) {
            if (e.which === 16) {
                that.state.shift = false;
            }
            if (e.which === 17) {
                that.state.ctrl = false;
            }
            that.handleKeyDown(e, that);
        };

    }

    componentWillReceiveProps(nextProps) {
        if(nextProps.location.pathname != this.props.location.pathname) {
            this.props.loading();
            this.props.fetchImages(nextProps.match.params.sort);
        }
    }

    handleEnterUp(e) {
        //ReactTestUtils.Simulate.click(e.target);
        //this.eventFire(e.target.firstChild, "click");
    }

    handleFocus(e) {
        this.adjustViewFocus(e);
        if(e.target.parentNode.classList.contains("selectable")) {
            this.handleSelection(e);
        }
    }

    adjustViewFocus(e) {

        if(e.detail.direction) { // only on keyboard events
            var scrollTo = e.target;
            var viewToV = scrollTo.parentElement.parentElement.parentElement;

            if(viewToV) {
                window.scrollTo(0, viewToV.offsetTop - 200);
            }
        }

    }


    handleSelection(e) {

        if(!this.state.ctrl && !this.state.shift) {
            this.selectedImages.forEach((element) => {
                this.imageNodes[element].setState({selected: false});
            });
            this.selectedImages = [];
        }

        const selectedImages = this.selectedImages;
        const newSelectedImages = [];
        const lastSelectedImage = document.getElementById(selectedImages[0]);
        if(this.state.shift && lastSelectedImage) {

            const range = document.createRange();
            range.setStart(lastSelectedImage, 0);
            range.setEnd(lastSelectedImage, 0);

            const relativity = range.comparePoint(e.target, 0);
            if(relativity === 1) {
                range.setEnd(e.target, 0);
            } else if (relativity === -1) {
                range.setStart(e.target, 0);
                range.setEnd(lastSelectedImage, 0);
            }

            var nodes = range.cloneContents().querySelectorAll(".selectable");
            for(var i=0;i<nodes.length;i++)
            {
                const elmId = nodes[i].parentNode.id;
                if(elmId && !selectedImages.includes( elmId)) {
                    newSelectedImages.push( elmId);
                }
            }

        } else {
            const id = e.target.firstChild.dataset.id;
            if(!selectedImages.includes(id)) {
                newSelectedImages.push(id);
            } else {
                selectedImages.splice(selectedImages.indexOf(id), 1);
                this.imageNodes[id].setState({selected: false});
            }
        }
        //console.log("select ...")
        newSelectedImages.forEach((element) => {
            this.imageNodes[element].setState({selected: true});
        });
        this.selectedImages = selectedImages.concat(newSelectedImages);

    }

    handleKeyDown(e, that) {
        if(e.keyCode === 84) { // "t" for "edit tag"
            that.tagModal.open(that.selectedImages);
        }
    }

    handleModalClose(e) {
        console.log("handleModalClose");
        const requeststs = [];
        this.selectedImages.forEach((element) => {
            requeststs.push(
                axios.get(apiBase + "api/images/" + element)
            );
        });

        axios.all(requeststs).then(res => {

            res.forEach((element) => {
                this.imageNodes[element.data.id].setState({tags: element.data.tags});
            });
              
        });            

    }

    handleClick(e) {

        const data = e.target.dataset;
        const images = this.props.images[data.path];
        this.refs.lightbox.setState({
            images: images,
            photoIndex: Object.keys(images).indexOf(data.image),
            headline: data.path,
            isOpen:true
        });

    }

    registerRowInternalNode(rowId, rowNode) {
        this.rowNodes[rowId] = rowNode;
    }

    registerImageInternalNode(imageId, imageNode) {
        this.imageNodes[imageId] = imageNode;
    }

    render() {

        const entries = this.props.images;
        const isTagsView = this.state.isTagsView;

        if(!entries) {
            return (
                <div>
                    <NavBar />
                    <Grid className="big-top-margin">
                        <Row>
                            <Col md={6} mdPush={3}>
                            <Jumbotron>
                                <div className="centered">
                                    <h1>Loading </h1>
                                    <Wave size={50} columns={15}/>
                                </div>
                            </Jumbotron>
                            </Col>
                        </Row>
                    </Grid>
                </div>
            );
        }


        return (
            <div>
                <SpatialNavigation>

                    <NavBar/>

                    <LightImageBox ref="lightbox" />

                    <Grid fluid className="images">

                            {Object.keys(entries).map(key =>
                                <LazyRow
                                    key={key}
                                    rownName={key}
                                    images={entries[key]}
                                    isTagsView={isTagsView}
                                    onFocus={this.handleFocus.bind(this)}
                                    onKeyDown={this.handleKeyDown.bind(this)}
                                    onChange={e => this.handleMenuEvent("tags", e)}
                                    onClick={this.handleClick.bind(this)}
                                    registerRow={this.registerRowInternalNode.bind(this)}
                                    registerImage={this.registerImageInternalNode.bind(this)}
                                />
                            )}

                    </Grid>

                    <TagModal ref={(tagModal) => { this.tagModal = tagModal; }}
                              onClose={this.handleModalClose.bind(this)} />

                </SpatialNavigation>
            </div>
        );

    }

}

function mapStateToProps(state) {
    return {images: state.images};
}

export default withRouter(connect(mapStateToProps, {fetchImages, loading})(App));


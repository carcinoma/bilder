import React, { Component } from 'react';
import Lightbox from 'react-image-lightbox';
import screenfull from 'screenfull';
import {apiBase} from '../constants'

export default class LightboxExample extends Component {

    constructor(props) {
        super(props);

        this.state = {
            photoIndex: 0,
            isOpen: false,
            headline: '',
            images: []
        };
        this.keys = [];
    }

    handleMax(e) {

            if (screenfull.enabled) {
                screenfull.toggle(document.getElementsByClassName("ReactModalPortal")[0]);
            }

    }

    handleMaxKey(e) {
        if(e.keyCode === 13) { // enter
            this.handleMax(e);
        }        
    }

    timer(e) {

        this.setState({
          photoIndex: (this.state.photoIndex + 1) % this.keys.length
        })

    }

    handlePlay(e) {

        if(!this.intervalId) {
            this.play(e);
        } else {
            this.stop(e);
        }

    }

    handlePlayKey(e) {
        if(e.keyCode === 13) { // enter
            this.handlePlay(e);
        }        
    }    

    setState(state) {
        if(state.images) {
            this.keys=Object.keys( state.images );
        }
        super.setState(state);
    }

    play(e) {
        this.intervalId = setInterval(this.timer.bind(this), 5000);
        if(e) {
            e.target.innerHTML='stop';
        }
    }

    stop(e) {
        clearInterval(this.intervalId);
        if(e) {
            e.target.innerHTML='play_arrow';
        }
        this.intervalId = null;
    }

    componentWillUnmount(){
        this.stop();
    }

    handleClose() {
        this.stop();
        //window.SpatialNavigation.focus();
        this.setState({ isOpen: false });
    }

    render() {
        const {
            photoIndex,
            isOpen,
            images,
            headline
        } = this.state;

        const currentKey=this.keys[photoIndex];
        const nextKey=this.keys[(photoIndex + 1) % this.keys.length];
        const prevKey=this.keys[(photoIndex + this.keys.length - 1) % this.keys.length];
        
        return (
            <div>
                {isOpen &&
                    <Lightbox ref="lightbox" 
                        mainSrc={`${apiBase}${images[currentKey].img}`}
                        nextSrc={`${apiBase}${images[nextKey].img}`}
                        prevSrc={`${apiBase}${images[prevKey].img}`}
                        mainSrcThumbnail={`${apiBase}${images[currentKey].thumb}`}
                        nextSrcThumbnail={`${apiBase}${images[nextKey].thumb}`}
                        prevSrcThumbnail={`${apiBase}${images[prevKey].thumb}`}
                        toolbarButtons={[
                            <div className="ml-1" 
                                 onClick={this.handleMax.bind(this)}
                                 onKeyUp={this.handleMaxKey.bind(this)}>
                                 <i tabIndex="0" className="material-icons">zoom_out_map</i>
                            </div>,
                            <div className="ml-1" 
                                 onClick={this.handlePlay.bind(this)}
                                 onKeyUp={this.handlePlayKey.bind(this)}>
                                 <i tabIndex="0" className="material-icons">play_arrow</i>
                            </div>,
                        ]}
                        imageTitle={<h4><strong>{headline}</strong>&nbsp;-&nbsp;{currentKey}&nbsp;<small>{photoIndex+1}/{this.keys.length}</small></h4>}
                        imageCaption={images[currentKey].tags.join(', ')}
                        imagePadding={45}
                        onCloseRequest={this.handleClose.bind(this)}
                        onMovePrevRequest={() => this.setState({
                            photoIndex: (photoIndex + this.keys.length - 1) % this.keys.length,
                        })}
                        onMoveNextRequest={() => this.setState({
                            photoIndex: (photoIndex + 1) % this.keys.length,
                        })}
                    ></Lightbox>
                }
            </div>
        );
    }
}
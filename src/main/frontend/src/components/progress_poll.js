import React, { Component } from 'react';
import { Line } from 'rc-progress';
import {apiBase} from '../constants'
import axios from 'axios';
import NavItem from 'react-bootstrap/lib/NavItem';

export default class ProgressPoll extends Component {

    constructor(props) {
        super(props);

        this.state = {
            total: '0',
            current: '0',
            currentFile: '',
            pollspeed: 10000
        };
    }
    componentDidMount() {
        this.startPolling();
      }
      
    componentWillUnmount() {
        this.stopPoll();
    }

    stopPoll() {
        if (this._timer) {
            clearInterval(this._timer);
            this._timer = null;
        }
    }

    setState(props) {

        if(!props.pollspeed && this.state.total != props.total) {
            if(props.total == 0 || props.total == props.current) {
                props.pollspeed = 10000;
            } else {
                props.pollspeed = 1000;
            }
        }

        var adjustPoll=false;
        if(props.pollspeed && props.pollspeed != this.state.pollspeed) {
            adjustPoll=true;
        }

        super.setState(props);

        if(adjustPoll) {
            this.adjustPolling();
        }
    }

    adjustPolling() {
        this.stopPoll();
        this._timer = setInterval(this.poll.bind(this), this.state.pollspeed);
    }

    startPolling() {
        var self = this;
        setTimeout(function() {
            self.poll(); // do it once and then start it up ...
            self._timer = setInterval(self.poll.bind(self), self.state.pollspeed);
        }, 1000);
    }

    poll() {
        console.log("polling progress");
        axios.get(apiBase + 'api/progress.json')
            .then(res => {
                const progress = res.data;
                this.setState({ 
                    total: progress.total, 
                    current: progress.current,
                    currentFile: progress.currentFile
                 });
        });
    }

    handleMenuEvent(eventKey, e) {

        let url='api/update-images.sh';
        axios.get(apiBase + url);
        this.setState({pollSpeed: 1000});
    }
    
    render() {
        const {
            total,
            current,
            currentFile
        } = this.state;

        if(total > 0) {

            return (   
                <li>         
                    <div style={{color: '#ddd', textAlign: 'right'}}>
                        <div>
                            <Line percent={current*100/total} 
                                strokeWidth="2" 
                                strokeColor="#DDD" 
                                trailColor="#777"
                                style={{width: '150px', marginTop: '15px'}} />
                        </div>
                        <div className="variant">
                            {currentFile}
                        </div>
                    </div>
                </li>
            );

        } else {

            return(
                <NavItem className="focusable-second zoom_out"
                        onSelect={this.handleMenuEvent.bind(this)}
                        data-eventkey={5}
                        eventKey={5} 
                        href="#">
                    <i className="material-icons">autorenew</i>
                </NavItem>
            );

        }

    }
}
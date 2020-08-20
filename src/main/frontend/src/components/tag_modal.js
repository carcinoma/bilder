import Modal from 'react-bootstrap/lib/Modal';
import Button from 'react-bootstrap/lib/Button';
import axios from 'axios';
import {apiBase} from '../constants'
import React, { Component } from 'react';
import 'react-select/dist/react-select.css';
import { Creatable } from 'react-select';

export default class TagModal extends Component {

    constructor(props) {
        super(props);
        this.firstRender=true;
        this.state = {
          showModal: false,
          tags: [],
          selectedImages: null,
          options: []
        };
    }

    close() {
        this.setState({ showModal: false });
    }

    save() {

        const requests = [];
        if(this.state.selectedImages.length === 1) {

            const tagList = [];
            for(let [key, value] of Object.entries(this.state.tags)) {
                tagList.push(value.value);
            };
    
            axios.put(apiBase + "api/images/" + this.state.selectedImages[0] + "/tags", tagList).then((res) => {
                this.finish();
            });

        } else {

            const createTagRequests = [];
            this.state.tags.forEach((tag) => {
                if(tag.className === "Select-create-option-placeholder") {
                    createTagRequests.push(axios.post(apiBase + "api/tags", {
                        name: tag.value
                    }))
                }
            });

            axios.all(createTagRequests).then((res) => {
                this.state.tags.forEach((tag) => {
                    requests.push(this.applyTags(tag));
                });

                axios.all(requests).then((res) => {
                    console.log("finish");
                    this.finish();
                });
                        
            });

        }
    }

    finish() {

        if(this.props.onClose) {
            this.props.onClose(this.state.tags);
        }
        this.close();

    }

    applyTags(tag) {

        const images = [];
        this.state.selectedImages.forEach((image) => {
            images.push({
                id: image
            });
        });
        return axios.post(apiBase + "api/tags/" + tag.value, images);

    }

    open(e) {

        axios.get(apiBase + "api/tags").then(res => {

            const tagList = {};
            for(let [key] of Object.entries(res.data)) {
                tagList[key] = ({value: key, label: key});
            };


            const requests = [];
            e.forEach((element) => {
                requests.push(
                    axios.get(apiBase + "api/images/" + element)
                );
            });

            axios.all(requests).then(res => {

                const resultingTagList = this.reduceTagList(tagList, res);
                const tagArrayList = [];
                for(let [key] of Object.entries(tagList)) {
                    tagArrayList.push({value: key, label: key});
                }   

                this.setState({
                    tags: resultingTagList,
                    options: tagArrayList,
                    selectedImages: e, 
                    showModal: true 
                });                 
            });

        });

    }

    reduceTagList(tagList, res) {

        let distinctTagsList = tagList;

        res.forEach((element) => {
            const reducedTagList = {};
            for(let [tagKey] of Object.entries(distinctTagsList)) {
                for(let [imageTagKey, imageTagValue] of Object.entries(element.data.tags)) {
                    if(tagKey === imageTagValue && !reducedTagList[tagKey]) {
                        reducedTagList[tagKey] = {value: tagKey, label: tagKey};
                    }
                };
            };
            distinctTagsList = reducedTagList;
        });

        const resultingTagList = [];
        for(let [key] of Object.entries(distinctTagsList)) {
            resultingTagList.push({value: key, label: key});
        }  

        return resultingTagList;

    }

    handleChange(event) {
        this.setState({tags: event.target.value});
    }

    logChange(val) {
        this.setState({tags: val});

    }
          
    render() {

        return(

            <div className="static-modal">
                <Modal show={this.state.showModal} onHide={this.close.bind(this)}>
                    <Modal.Header>
                        <Modal.Title>Image Tags</Modal.Title>
                    </Modal.Header>
            ​
                    <Modal.Body>
                        <Creatable
                            className="focusable"
                            name="tags"
                            value={this.state.tags}
                            options={this.state.options}
                            onChange={this.logChange.bind(this)}
                            multi={true}
                            />                        
                     </Modal.Body>
            ​
                    <Modal.Footer>
                        <Button onClick={this.close.bind(this)}>Close</Button>
                        <Button onClick={this.save.bind(this)} bsStyle="primary">Save changes</Button>
                    </Modal.Footer>
            ​
                </Modal>
            </div>    

        );        
    }
        
}
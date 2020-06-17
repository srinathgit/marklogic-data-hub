import React, {useState, useContext} from 'react';
import styles from './custom-card.module.scss';
import {Card, Icon, Tooltip, Row, Col, Modal, Select} from 'antd';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {faTrashAlt} from '@fortawesome/free-regular-svg-icons';
import { getInitialChars } from '../../../util/conversionFunctions';
import AdvancedSettingsDialog from "../../advanced-settings/advanced-settings-dialog";
import { AdvMapTooltips } from '../../../config/tooltips.config';
import ViewCustomDialog from "./view-custom-dialog/view-custom-dialog";


interface Props {
    data: any;
    entityTypeTitle: any;
}

const CustomCard: React.FC<Props> = (props) => {
    const activityType = 'custom';
    const [openCustomSettings, setOpenCustomSettings] = useState(false);
    const [customData, setCustomData] = useState({});
    const [viewCustom, setViewCustom] = useState(false);

    const OpenCustomDialog = (index) => {
        setViewCustom(true);
        setCustomData(prevState => ({ ...prevState, ...props.data[index]}));
    }

    const OpenCustomSettingsDialog = (index) => {
        setCustomData(prevState => ({ ...prevState, ...props.data[index]}));
        setOpenCustomSettings(true);
        console.log('Open settings')
    }

    return (
        <div className={styles.customContainer}>
            <Row gutter={16} type="flex" >
                {props && props.data.length > 0 ? props.data.map((elem,index) => (
                <Col key={index}>
                    <Card
                        actions={[
                            <span></span>,
                            <Tooltip title={'Settings'} placement="bottom"><Icon type="setting" key="setting" role="settings-custom button" data-testid={elem.name+'-settings'} onClick={() => OpenCustomSettingsDialog(index)}/></Tooltip>,
                            <Tooltip title={'Edit'} placement="bottom"><Icon type="edit" key="edit" role="edit-custom button" data-testid={elem.name+'-edit'} onClick={() => OpenCustomDialog(index)}/></Tooltip>,
                            <i role="disabled-delete-custom button" onClick={(event) => event.preventDefault()}><FontAwesomeIcon icon={faTrashAlt} className={styles.disabledDeleteIcon} size="lg"/></i>,
                        ]}
                        className={styles.cardStyle}
                        size="small"
                    >
                    <span className={styles.customNameStyle}>{getInitialChars(elem.name, 27, '...')}</span>
                    <br />
                    {<div className={styles.sourceQuery}>Source Query: {getInitialChars(elem.sourceQuery,32,'...')}</div>}
                    <br /><br />
                    </Card>
                </Col>
            )) : <span></span> }</Row>
            <ViewCustomDialog
                targetEntityType={props.entityTypeTitle.entityTypeId}
                viewCustom={viewCustom}
                setViewCustom={setViewCustom}
                customData={customData}
                canReadWrite={false}/>
            <AdvancedSettingsDialog
                tooltipsData={AdvMapTooltips}
                openAdvancedSettings={openCustomSettings}
                setOpenAdvancedSettings={setOpenCustomSettings}
                stepData={customData}
                activityType={activityType}
                canWrite={false}
            />
        </div>
    );

}

export default CustomCard;

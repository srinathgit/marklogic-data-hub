import React, { useState } from 'react';
import { MLRadio } from '@marklogic/design-system';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faThLarge, faTable } from '@fortawesome/free-solid-svg-icons';
import './switch-view.scss';

interface Props {
    handleSelection: any,
    defaultView: string,
}

const SwitchView: React.FC<Props> = (props) => {
    let [view, setView] = useState(props.defaultView);

    const onChange = (val) => {
        setView(val);
        props.handleSelection(val);
    };

    return (
        <div id="switch-view" aria-label="switch-view">
            <MLRadio.MLGroup
                buttonStyle="outline"
                defaultValue={view}
                name="radiogroup"
                onChange={e => onChange(e.target.value)}
                size="large"
            >
                <MLRadio.MLButton aria-label="switch-view-card" value={'card'}>
                    <i>{<FontAwesomeIcon icon={faThLarge} />}</i>
                </MLRadio.MLButton>
                <MLRadio.MLButton aria-label="switch-view-list" value={'list'}>
                    <i>{<FontAwesomeIcon icon={faTable} />}</i>
                </MLRadio.MLButton>
            </MLRadio.MLGroup>
        </div>

    );
};

export default SwitchView;

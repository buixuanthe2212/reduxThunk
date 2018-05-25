// @flow
import React, { Component } from 'react';
import { View, TouchableOpacity, Text } from 'react-native';
import { connect } from 'react-redux';
import { styles } from './styles';

type Props = {
  item: any,
};

export class ItemExpenseView extends Component<Props> {
  render() {
    // const { item, index } = this.props.item;
    const { total, subject } = this.props.item;

    return (
      <View>
        <TouchableOpacity style={styles.container_item} onPress={() => {}}>
          <View style={styles.container_item}>
            <View style={styles.item_view_top}>
              <Text
                style={styles.text_item}
              >{`申請者（代理:  ${subject}`}</Text>

              <Text style={styles.text_item}>{`金額 ${total}`}</Text>
            </View>
            <View style={styles.item_view_bottom}>
              <Text style={styles.text_item}>{`件名} ${subject}`}</Text>

              <Text style={styles.text_progress}>次承認中</Text>
            </View>
          </View>
        </TouchableOpacity>

        <View style={styles.view_line_bottom} />
      </View>
    );
  }
}

const mapDispatchToProps = () => ({});

const mapStatesToProps = () => ({});

export const ItemExpense = connect(mapStatesToProps, mapDispatchToProps)(
  ItemExpenseView,
);

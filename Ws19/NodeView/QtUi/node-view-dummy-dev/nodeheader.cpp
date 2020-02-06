#include "nodeheader.h"

#include <QtWidgets>

NodeHeader::NodeHeader(QGraphicsItem *parent, QString _name, QColor _text_color, qreal _width, qreal _height)
{
    Q_UNUSED(parent)

    name = _name;
    text_color = _text_color;
    width = _width - 2;
    height = 26;

    offset_x = (_width - width)/2;
    offset_y = 2;
}

QRectF NodeHeader::boundingRect() const
{
    return QRectF(offset_x, offset_y, width, height);
}

void NodeHeader::paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget)
{
    Q_UNUSED(widget)
    Q_UNUSED(option)

    QPen pen(text_color, 2);
    QFont font("Ubuntu", 12);
    font.setStyleStrategy(QFont::ForceOutline);
    painter->setFont(font);
    painter->setPen(pen);
    painter->drawText(static_cast<int>(offset_x),
                      static_cast<int>(offset_y),
                      static_cast<int>(width),
                      static_cast<int>(height),
                      Qt::AlignCenter, name);
}



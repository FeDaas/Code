#include "nodebody.h"

#include <QtWidgets>

NodeBody::NodeBody(QGraphicsItem *parent, QColor _color, qreal _width, qreal _height)
{
    Q_UNUSED(parent)

    color = _color;
    width = _width;
    height = _height;
}

QRectF NodeBody::boundingRect() const
{
    return QRectF(0, 0, width, height);
}

void NodeBody::paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget)
{
    Q_UNUSED(widget)

    QRectF rec = boundingRect();
    QColor darker_color = color.darker(150);
    QColor lighter_color = color.lighter(150);
    darker_color.setAlpha(255);
    lighter_color.setAlpha(255);

    QColor outlinecolor = (option->state & QStyle::State_Selected) ? Qt::white : lighter_color;
    painter->setRenderHint(QPainter::Antialiasing);

    QPainterPath path;
    QPen pen(outlinecolor, 2);
    painter->setPen(pen);

    qreal scaled_height = 22;

    // Main Body
    path.addRoundedRect(rec, 10, 15);
    painter->fillPath(path, color);
    painter->drawPath(path);

    // Upper Field Shadow
    path.clear();
    path.addRect(1, 12, rec.width() - 2, scaled_height);
    painter->fillPath(path, darker_color.darker(150));

    // Upper Field
    path.clear();
    path.addRoundedRect(0, 0, rec.width(), scaled_height, 10, 15);
    painter->fillPath(path, darker_color);
    painter->drawPath(path);

    path.clear();
    pen.setColor(darker_color);
    painter->setPen(pen);
    path.addRect(1, 8, rec.width() - 2, scaled_height);
    painter->fillPath(path, darker_color);

    // Lower Field
    // path.clear();
    // path.addRoundedRect(0, rec.height() * 0.8, rec.width(), rec.height() * 0.2, 10, 15);
    // painter->fillPath(path, darker_color);
    // painter->drawPath(path);

    // path.clear();
    // pen.setColor(darker_color);
    // painter->setPen(pen);
    // path.addRect(1, rec.height() * 0.8, rec.width() - 2, rec.height() * 0.2);
    // painter->fillPath(path, darker_color);
}

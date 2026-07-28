import { classNames } from '../utils/classNames';

export default function Table({ columns, rows, emptyMessage = 'No records available.', className = '' }) {
  return (
    <div className={classNames('table-shell', className)}>
      <table className="table">
        <thead>
          <tr>
            {columns.map(column => (
              <th key={column.key} style={{ textAlign: column.align || 'left' }}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length > 0 ? (
            rows.map((row, rowIndex) => (
              <tr key={row.id ?? rowIndex}>
                {columns.map(column => (
                  <td key={column.key} style={{ textAlign: column.align || 'left' }}>
                    {column.render ? column.render(row) : row[column.key]}
                  </td>
                ))}
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={columns.length} className="table__empty">
                {emptyMessage}
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
